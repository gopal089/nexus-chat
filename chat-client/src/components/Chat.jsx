import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import axios from 'axios';
import { LogOut, Send, User, MessageCircle } from 'lucide-react';
import styles from './Chat.module.css';

const API_BASE = import.meta.env.VITE_API_URL ? import.meta.env.VITE_API_URL.replace('/api/auth', '') : 'http://localhost:8081';
const SOCKET_BASE = `${API_BASE}/ws`;

export default function Chat() {
    const [messages, setMessages] = useState([]);
    const [inputMessage, setInputMessage] = useState('');
    const [stompClient, setStompClient] = useState(null);
    const [isConnected, setIsConnected] = useState(false);
    const [rooms, setRooms] = useState([]);
    const [activeRoom, setActiveRoom] = useState(null); // { id, name }

    const navigate = useNavigate();
    const username = localStorage.getItem('chat_username');
    const token = localStorage.getItem('chat_token');
    const messagesEndRef = useRef(null);
    const stompRef = useRef(null); // stable ref for cleanup

    // ── Step 1: Fetch rooms from REST, pick global-chat ────────────────
    useEffect(() => {
        if (!token || !username) { navigate('/'); return; }

        axios.get(`${API_BASE}/api/rooms`)
            .then(res => {
                const fetched = res.data;
                console.log('Fetched rooms:', fetched);
                setRooms(fetched);
                const global = fetched.find(r => r.name === 'global-chat') || fetched[0];
                if (global) {
                    console.log('Setting active room:', global.name, global.id);
                    setActiveRoom({ id: global.id, name: global.name });
                }
            })
            .catch(err => console.error('Failed to fetch rooms:', err));
    }, [navigate, token, username]);

    // ── Step 2: Connect WebSocket once we have a room ──────────────────
    useEffect(() => {
        if (!activeRoom || !token) return;

        // Deactivate any previous client
        if (stompRef.current) { stompRef.current.deactivate(); }

        const socketUrl = `${SOCKET_BASE}?token=${encodeURIComponent(token)}`;
        const client = new Client({
            webSocketFactory: () => new SockJS(socketUrl),
            debug: () => { },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = () => {
            setIsConnected(true);

            // Subscribe to the room's topic
            const topic = `/topic/room.${activeRoom.id}`;
            console.log('Subscribing to:', topic);
            client.subscribe(topic, (msg) => {
                console.log('Received message:', msg.body);
                const body = JSON.parse(msg.body);
                setMessages(prev => [...prev, body]);
                scrollToBottom();
            });

            // Announce join with the correct roomId
            client.publish({
                destination: '/app/chat.join',
                body: JSON.stringify({ roomId: activeRoom.id }),
            });
        };

        client.onDisconnect = () => setIsConnected(false);
        client.onStompError = (frame) => {
            console.error('STOMP error:', frame.headers['message'], frame.body);
            setIsConnected(false);
        };

        client.activate();
        stompRef.current = client;
        setStompClient(client);
        setMessages([]); // clear messages when switching rooms

        return () => { client.deactivate(); };
    }, [activeRoom, token]);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const handleSendMessage = (e) => {
        e.preventDefault();
        if (inputMessage.trim() && stompClient && isConnected && activeRoom) {
            stompClient.publish({
                destination: '/app/chat.send',
                body: JSON.stringify({
                    roomId: activeRoom.id,
                    content: inputMessage,
                    messageType: 'TEXT',
                }),
            });
            setInputMessage('');
        }
    };

    const handleSwitchRoom = (room) => {
        setActiveRoom({ id: room.id, name: room.name });
    };

    const handleLogout = () => {
        stompRef.current?.deactivate();
        localStorage.removeItem('chat_token');
        localStorage.removeItem('chat_username');
        navigate('/');
    };

    return (
        <div className={styles.appContainer}>
            {/* Sidebar */}
            <aside className={styles.sidebar}>
                <div className={styles.sidebarHeader}>
                    <div className={styles.appBrand}>
                        <div className={styles.iconWrapper}><MessageCircle size={24} /></div>
                        <h2>Nexus Chat</h2>
                    </div>
                </div>

                <div className={styles.sidebarContent}>
                    <div className={styles.sectionTitle}>ROOMS</div>
                    {rooms.map(room => (
                        <div
                            key={room.id}
                            className={`${styles.roomItem} ${activeRoom?.id === room.id ? styles.activeRoom : ''}`}
                            onClick={() => handleSwitchRoom(room)}
                        >
                            <span className={styles.hashtag}>#</span> {room.name}
                        </div>
                    ))}
                </div>

                <div className={styles.sidebarFooter}>
                    <div className={styles.userProfile}>
                        <div className={styles.avatar}><User size={20} /></div>
                        <div className={styles.userInfo}>
                            <span className={styles.userName}>{username}</span>
                            <span className={styles.status}>
                                <span className={isConnected ? styles.dotGreen : styles.dotRed}></span>
                                {isConnected ? 'Online' : 'Reconnecting...'}
                            </span>
                        </div>
                        <button onClick={handleLogout} className={styles.logoutBtn} aria-label="Logout">
                            <LogOut size={18} />
                        </button>
                    </div>
                </div>
            </aside>

            {/* Main Chat Area */}
            <main className={styles.mainChat}>
                <header className={styles.chatHeader}>
                    <div className={styles.chatTitle}>
                        <span className={styles.hashtag}>#</span>
                        <h3>{activeRoom?.name ?? 'Loading...'}</h3>
                    </div>
                </header>

                <div className={styles.messageList}>
                    {messages.length === 0 && (
                        <div className={styles.emptyState}>
                            <MessageCircle size={48} />
                            <p>Welcome to the beginning of the #{activeRoom?.name} channel.</p>
                        </div>
                    )}

                    {messages.map((msg, idx) => {
                        const isMe = msg.senderUsername === username;

                        if (msg.messageType === 'SYSTEM' || msg.senderUsername === 'SYSTEM') {
                            return (
                                <div key={idx} className={styles.systemMessage}>
                                    {msg.content}
                                </div>
                            );
                        }

                        return (
                            <div key={idx} className={`${styles.messageWrapper} ${isMe ? styles.messageMine : ''}`}>
                                {!isMe && (
                                    <div className={styles.messageAvatar}>
                                        {msg.senderUsername?.charAt(0).toUpperCase()}
                                    </div>
                                )}
                                <div className={styles.messageContent}>
                                    {!isMe && <span className={styles.messageSender}>{msg.senderUsername}</span>}
                                    <div className={`${styles.bubble} ${isMe ? styles.bubbleMine : styles.bubbleTheirs}`}>
                                        {msg.content}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                    <div ref={messagesEndRef} />
                </div>

                <div className={styles.inputArea}>
                    <form onSubmit={handleSendMessage} className={styles.inputForm}>
                        <input
                            type="text"
                            placeholder={`Message #${activeRoom?.name ?? '...'}...`}
                            value={inputMessage}
                            onChange={(e) => setInputMessage(e.target.value)}
                            disabled={!isConnected}
                        />
                        <button
                            type="submit"
                            disabled={!inputMessage.trim() || !isConnected}
                            className={styles.sendButton}
                        >
                            <Send size={18} />
                        </button>
                    </form>
                </div>
            </main>
        </div>
    );
}

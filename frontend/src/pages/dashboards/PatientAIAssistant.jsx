import React, { useState } from 'react';
import axios from 'axios';

const PatientAIAssistant = () => {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSend = async () => {
        if (!input.trim()) return;
        
        const userMessage = { role: 'user', content: input };
        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setLoading(true);

        try {
            const token = localStorage.getItem('token');
            const res = await axios.post('http://localhost:8080/api/ai/chat', 
                { message: userMessage.content },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            
            setMessages(prev => [...prev, { role: 'assistant', content: res.data.response }]);
        } catch (err) {
            setMessages(prev => [...prev, { role: 'assistant', content: "Sorry, I am unable to connect to the server right now." }]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-white p-6 rounded shadow max-w-2xl mx-auto h-[600px] flex flex-col">
            <h2 className="text-xl font-bold mb-2">LifeLink AI Assistant</h2>
            <p className="text-xs text-gray-500 mb-4 bg-yellow-50 p-2 rounded">
                AI-generated information. This assistant does not diagnose medical conditions or replace professional medical care.
            </p>
            
            <div className="flex-1 overflow-y-auto mb-4 border rounded p-4 space-y-4">
                {messages.length === 0 && (
                    <div className="text-center text-gray-400 mt-10">
                        Ask me about first-aid, healthcare info, or emergency guidance.
                    </div>
                )}
                {messages.map((m, i) => (
                    <div key={i} className={`p-3 rounded-lg max-w-[80%] ${m.role === 'user' ? 'bg-blue-100 ml-auto' : 'bg-gray-100'}`}>
                        {m.content}
                    </div>
                ))}
                {loading && <div className="text-gray-400 italic">Thinking...</div>}
            </div>
            
            <div className="flex gap-2">
                <input 
                    type="text" 
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleSend()}
                    className="flex-1 border rounded px-3 py-2"
                    placeholder="Type your question..."
                />
                <button onClick={handleSend} disabled={loading} className="bg-blue-600 text-white px-4 py-2 rounded">
                    Send
                </button>
            </div>
        </div>
    );
};

export default PatientAIAssistant;
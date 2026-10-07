import React, { useState } from 'react';
import axios from 'axios';

const AdminAIAssistant = () => {
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
            const res = await axios.post('http://localhost:8080/api/ai/admin-query', 
                { query: userMessage.content },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            
            setMessages(prev => [...prev, { role: 'assistant', content: res.data.response, disclaimer: res.data.disclaimer }]);
        } catch (err) {
            const errorMsg = err.response?.data?.message || err.message || "Unknown error";
            setMessages(prev => [...prev, { role: 'assistant', content: `Error: ${errorMsg}` }]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-white p-6 rounded shadow max-w-3xl mx-auto h-[600px] flex flex-col mt-6">
            <h2 className="text-xl font-bold mb-4">Admin AI Analytics</h2>
            
            <div className="flex-1 overflow-y-auto mb-4 border rounded p-4 space-y-4">
                {messages.length === 0 && (
                    <div className="text-center text-gray-400 mt-10">
                        Ask analytics questions like "What is the current number of available beds?"
                    </div>
                )}
                {messages.map((m, i) => (
                    <div key={i} className={`p-3 rounded-lg max-w-[80%] ${m.role === 'user' ? 'bg-indigo-100 ml-auto' : 'bg-gray-100'}`}>
                        <div>{m.content}</div>
                        {m.disclaimer && <div className="text-xs text-gray-400 mt-2 italic">{m.disclaimer}</div>}
                    </div>
                ))}
                {loading && <div className="text-gray-400 italic">Querying system...</div>}
            </div>
            
            <div className="flex gap-2">
                <input 
                    type="text" 
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleSend()}
                    className="flex-1 border rounded px-3 py-2"
                    placeholder="Ask about system analytics..."
                />
                <button onClick={handleSend} disabled={loading} className="bg-indigo-600 text-white px-4 py-2 rounded">
                    Query
                </button>
            </div>
        </div>
    );
};

export default AdminAIAssistant;
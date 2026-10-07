import React, { useState } from 'react';
import axios from 'axios';

const DoctorAISummary = ({ patientId }) => {
    const [summaryData, setSummaryData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const generateSummary = async () => {
        if (!patientId) return;
        setLoading(true);
        setError(null);
        try {
            const token = localStorage.getItem('token');
            const res = await axios.post(`http://localhost:8080/api/ai/medical-summary/${patientId}`, 
                {},
                { headers: { Authorization: `Bearer ${token}` } }
            );
            setSummaryData(res.data);
        } catch (err) {
            setError("Failed to generate AI summary.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-white p-6 rounded shadow mt-6">
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold text-blue-900">AI Medical Summary</h2>
                <button 
                    onClick={generateSummary} 
                    disabled={loading || !patientId}
                    className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:bg-blue-300"
                >
                    {loading ? 'Generating...' : 'Generate AI Summary'}
                </button>
            </div>
            
            {error && <div className="text-red-500 mb-4">{error}</div>}
            
            {summaryData && (
                <div className="space-y-4 border-t pt-4">
                    <div>
                        <h3 className="font-semibold text-gray-700">Summary</h3>
                        <p className="text-gray-600">{summaryData.summary}</p>
                    </div>
                    
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <h3 className="font-semibold text-gray-700">Allergies</h3>
                            <ul className="list-disc pl-5 text-gray-600">
                                {summaryData.allergies?.map((a, i) => <li key={i}>{a}</li>)}
                                {(!summaryData.allergies || summaryData.allergies.length === 0) && <li>None documented</li>}
                            </ul>
                        </div>
                        <div>
                            <h3 className="font-semibold text-gray-700">Current Medications</h3>
                            <ul className="list-disc pl-5 text-gray-600">
                                {summaryData.medications?.map((m, i) => <li key={i}>{m}</li>)}
                                {(!summaryData.medications || summaryData.medications.length === 0) && <li>None documented</li>}
                            </ul>
                        </div>
                    </div>
                    
                    {summaryData.missingInformation && summaryData.missingInformation.length > 0 && (
                        <div className="bg-yellow-50 p-3 rounded">
                            <h3 className="font-semibold text-yellow-800 text-sm">Missing Information</h3>
                            <ul className="list-disc pl-5 text-yellow-700 text-sm">
                                {summaryData.missingInformation.map((m, i) => <li key={i}>{m}</li>)}
                            </ul>
                        </div>
                    )}
                    
                    <div className="text-xs text-gray-400 mt-4 border-t pt-2">
                        {summaryData.disclaimer} | Model: {summaryData.model} | Generated at: {new Date(summaryData.generatedAt).toLocaleString()}
                    </div>
                </div>
            )}
        </div>
    );
};

export default DoctorAISummary;
import React from 'react';

const BloodBankDashboard = () => {
    return (
        <div className="p-6">
            <h1 className="text-3xl font-bold mb-6">Blood Bank Dashboard</h1>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-white p-6 rounded shadow">
                    <h2 className="text-xl font-semibold mb-4">Inventory</h2>
                    <p>Manage blood groups and units available.</p>
                </div>
                <div className="bg-white p-6 rounded shadow">
                    <h2 className="text-xl font-semibold mb-4">Incoming Requests</h2>
                    <p>Process incoming emergency blood requests.</p>
                </div>
            </div>
        </div>
    );
};

export default BloodBankDashboard;
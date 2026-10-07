import React from 'react';

const PharmacyDashboard = () => {
    return (
        <div className="p-6">
            <h1 className="text-3xl font-bold mb-6">Pharmacy Dashboard</h1>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-white p-6 rounded shadow">
                    <h2 className="text-xl font-semibold mb-4">Medicine Inventory</h2>
                    <p>Manage medicines, quantity, and price.</p>
                </div>
                <div className="bg-white p-6 rounded shadow">
                    <h2 className="text-xl font-semibold mb-4">Low Stock Alerts</h2>
                    <p>View medicines below threshold.</p>
                </div>
            </div>
        </div>
    );
};

export default PharmacyDashboard;
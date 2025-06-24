// src/components/SearchPanel.jsx

import React from 'react';

const SearchPanel = ({ onSearch, isLoading }) => {
    // Danh sách các truy vấn đã được định nghĩa sẵn
    const queries = [
        {
            label: "1. Hoạt động của user 'user_alice_101'",
            params: { field: 'subject_id', value: 'user_alice_101', isNested: false }
        },
        {
            label: "2. Tất cả đăng nhập thất bại",
            params: { field: 'action', value: 'UserLoginFailed', isNested: false }
        },
        {
            label: "3. Sự kiện từ IP '198.51.100.10'",
            params: { field: 'data.ip_address', value: '198.51.100.10', isNested: true }
        },
        {
            label: "4. Truy vết luồng 'corr_carol_session_ghi'",
            params: { field: 'correlation_id', value: 'corr_carol_session_ghi', isNested: false }
        },
    ];

    return (
        <div className="search-panel">
            <h3>Truy vấn</h3>
            {queries.map((query) => (
                <button
                    key={query.label}
                    onClick={() => onSearch(query.params, query.label)}
                    disabled={isLoading}
                >
                    {query.label}
                </button>
            ))}
        </div>
    );
};

export default SearchPanel;
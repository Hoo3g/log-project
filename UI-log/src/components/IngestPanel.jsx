// src/components/IngestPanel.jsx

import React from 'react';

const IngestPanel = ({ onIngest, isIngesting, ingestStatus }) => {
    return (
        <div className="ingest-panel">
            <h3>Quản lý Dữ liệu</h3>
            <button 
                onClick={onIngest} 
                disabled={isIngesting}
                title="Gửi yêu cầu nạp lại toàn bộ log từ PostgreSQL vào OpenSearch"
            >
                {isIngesting ? 'Đang nạp dữ liệu...' : 'Nạp dữ liệu từ DB'}
            </button>
            {/* Hiển thị thông báo trạng thái sau khi nạp */}
            {ingestStatus && <p className={`status-message ${ingestStatus.type}`}>{ingestStatus.message}</p>}
        </div>
    );
};

export default IngestPanel;
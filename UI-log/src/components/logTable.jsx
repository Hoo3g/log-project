// src/components/LogTable.jsx

import React from 'react';

const LogTable = ({ logs, isLoading }) => {
    // Hiển thị thông báo khi đang tải dữ liệu
    if (isLoading) {
        return <p>Đang tải dữ liệu...</p>;
    }

    // Hiển thị thông báo khi không có log hoặc chưa tìm kiếm
    if (!logs || logs.length === 0) {
        return <p>Không có dữ liệu log để hiển thị. Vui lòng chọn một truy vấn từ thanh bên.</p>;
    }

    // Các cột sẽ hiển thị trong bảng
    const headers = ['Thời gian', 'Chủ thể (ID)', 'Hành động', 'ID Tương quan', 'Đối tượng', 'Chi tiết (Data)'];

    return (
        <div className="log-table-container">
            <table>
                <thead>
                    <tr>
                        {headers.map(header => <th key={header}>{header}</th>)}
                    </tr>
                </thead>
                <tbody>
                    {logs.map((log, index) => (
                        <tr key={log.event_id || index}>
                            <td>{new Date(log['@timestamp']).toLocaleString('vi-VN')}</td>
                            <td>{log.subject_id}</td>
                            <td>{log.action}</td>
                            <td>{log.correlation_id}</td>
                            <td>{`${log.target_type}: ${log.target_id}`}</td>
                            <td>
                                {log.data ? (
                                    <pre>{JSON.stringify(log.data, null, 2)}</pre>
                                ) : (
                                    <i>Không có</i>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default LogTable;
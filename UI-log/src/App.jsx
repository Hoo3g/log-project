// src/App.jsx

import React, { useState } from 'react';
import SearchPanel from './components/SearchPanel.jsx';
import LogTable from './components/logTable.jsx';
import logService from './services/logService';
import IngestPanel from './components/IngestPanel.jsx';
import DynamicSearchPanel from './components/DynamicSearchPanel.jsx';
import './App.css'; // File CSS bạn đã viết

function App() {
    const [logs, setLogs] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [currentQuery, setCurrentQuery] = useState('Chào mừng đến với Dashboard Log!');

    const [isIngesting, setIsIngesting] = useState(false);
    const [ingestStatus, setIngestStatus] = useState(null);

    const handleSearch = async (params, queryLabel) => {
        setIsLoading(true);
        setError(null);
        setCurrentQuery(`Đang tìm... ${queryLabel}`);
        
         try {
            const response = await logService.searchLogs(params.field, params.value, params.isNested);
            setLogs(response.data);
            setCurrentQuery(queryLabel); // Sử dụng label động được truyền vào
        } catch (err) {
            console.error("Lỗi khi tìm kiếm log:", err);
            const errorMessage = err.response?.data?.error || "Không thể kết nối đến server.";
            setError(errorMessage);
            setLogs([]);
            setCurrentQuery(`Lỗi khi thực hiện truy vấn.`);
        } finally {
            setIsLoading(false);
        }
    };
     //  XỬ LÝ SỰ KIỆN CLICK NẠP DỮ LIỆU ---
    const handleIngest = async () => {
        setIsIngesting(true);
        setIngestStatus({ type: 'info', message: 'Đang gửi yêu cầu nạp dữ liệu...' });

        try {
            const response = await logService.ingestLogs();
            // Cập nhật thông báo thành công
            setIngestStatus({ type: 'success', message: response.data.message });
            // Gợi ý: có thể tự động tìm kiếm lại sau khi nạp xong
            // handleSearch({ field: 'action', value: 'UserLoggedIn' }, 'Người dùng đăng nhập gần đây');
        } catch (err) {
            console.error("Lỗi khi nạp dữ liệu:", err);
            const errorMessage = err.response?.data?.error || "Không thể nạp dữ liệu.";
            // Cập nhật thông báo thất bại
            setIngestStatus({ type: 'error', message: `Lỗi: ${errorMessage}` });
        } finally {
            setIsIngesting(false);
        }
    };
    return (
        <div className="App">
            <header className="App-header">
                <h1>Dashboard Log</h1>
            </header>
            <main>
                {/* Thanh điều khiển bên trái */}
                <div className="left-panel">
                    
                    <IngestPanel 
                        onIngest={handleIngest} 
                        isIngesting={isIngesting} 
                        ingestStatus={ingestStatus}
                    />
                    <SearchPanel onSearch={handleSearch} isLoading={isLoading} /> 
                </div>

                {/* Khu vực kết quả bên phải */}
                <div className="results-area">
                    <h2>{currentQuery}</h2>
                    {error && <p className="error">{error}</p>}
                    <LogTable logs={logs} isLoading={isLoading} />
                </div>
            </main>
        </div>
    );
}

export default App;
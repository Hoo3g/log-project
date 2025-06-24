// src/components/DynamicSearchPanel.jsx

import React, { useState } from 'react';

// Định nghĩa các trường mà người dùng có thể tìm kiếm
const searchableFields = [
    { 
        value: 'subject_id', 
        label: 'ID Chủ thể (User ID)', 
        isNested: false, 
        placeholder: 'ví dụ: user_alice_101' 
    },
    { 
        value: 'action', 
        label: 'Hành động (Action)', 
        isNested: false, 
        placeholder: 'ví dụ: UserLoginFailed' 
    },
    { 
        value: 'correlation_id', 
        label: 'ID Tương quan (Correlation ID)', 
        isNested: false, 
        placeholder: 'ví dụ: corr_carol_session_ghi' 
    },
    { 
        value: 'data.ip_address', 
        label: 'Địa chỉ IP (trong data)', 
        isNested: true, 
        placeholder: 'ví dụ: 198.51.100.10' 
    },
    { 
        value: 'target_id', 
        label: 'ID Đối tượng (Target ID)', 
        isNested: false, 
        placeholder: 'ví dụ: file_project_plan_v1' 
    },
];

const DynamicSearchPanel = ({ onSearch, isLoading }) => {
    // State để lưu trường và giá trị người dùng chọn
    const [selectedField, setSelectedField] = useState(searchableFields[0].value);
    const [searchValue, setSearchValue] = useState('');

    // Hàm xử lý khi người dùng nhấn nút tìm kiếm
    const handleSearchClick = () => {
        if (!searchValue.trim()) {
            alert('Vui lòng nhập giá trị để tìm kiếm.');
            return;
        }

        // Tìm thông tin đầy đủ của trường đã chọn (để lấy isNested)
        const fieldInfo = searchableFields.find(f => f.value === selectedField);
        
        const params = {
            field: fieldInfo.value,
            value: searchValue,
            isNested: fieldInfo.isNested,
        };
        
        // Tạo một label động cho kết quả
        const queryLabel = `Tìm kiếm '${fieldInfo.label}' với giá trị '${searchValue}'`;

        onSearch(params, queryLabel);
    };

    // Lấy placeholder động dựa trên trường đã chọn
    const currentPlaceholder = searchableFields.find(f => f.value === selectedField)?.placeholder || '';

    return (
        <div className="search-panel">
            <h3>Tìm kiếm tùy chỉnh</h3>
            <div className="form-group">
                <label htmlFor="search-field">Trường tìm kiếm:</label>
                <select 
                    id="search-field"
                    value={selectedField}
                    onChange={(e) => setSelectedField(e.target.value)}
                    disabled={isLoading}
                >
                    {searchableFields.map(field => (
                        <option key={field.value} value={field.value}>
                            {field.label}
                        </option>
                    ))}
                </select>
            </div>

            <div className="form-group">
                <label htmlFor="search-value">Giá trị:</label>
                <input
                    type="text"
                    id="search-value"
                    value={searchValue}
                    onChange={(e) => setSearchValue(e.target.value)}
                    placeholder={currentPlaceholder}
                    disabled={isLoading}
                    // Cho phép nhấn Enter để tìm kiếm
                    onKeyDown={(e) => e.key === 'Enter' && handleSearchClick()}
                />
            </div>

            <button 
                className="search-button"
                onClick={handleSearchClick}
                disabled={isLoading || !searchValue.trim()}
            >
                {isLoading ? 'Đang tìm...' : 'Tìm kiếm'}
            </button>
        </div>
    );
};

export default DynamicSearchPanel;
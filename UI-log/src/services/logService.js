// src/services/logService.js

import axios from 'axios';

// URL của backend Java đang chạy
const API_BASE_URL = 'http://localhost:8080/api/logs';

/**
 * Gọi API để tìm kiếm log.
 * @param {string} field - Tên trường cần tìm.
 * @param {string} value - Giá trị cần tìm.
 * @param {boolean} isNested - Trường có phải là nested hay không.
 * @returns {Promise} - Promise chứa dữ liệu từ Axios.
 */
const searchLogs = (field, value, isNested = false) => {
    return axios.get(`${API_BASE_URL}/search`, {
        params: {
            field,
            value,
            isNested
        }
    });
};

/**
 * Gửi yêu cầu POST để nạp dữ liệu từ database vào OpenSearch.
 * @returns {Promise}
 */
const ingestLogs = () => {
    // Với request POST, URL là tham số đầu tiên, body là tham số thứ hai (ở đây không có body).
    return axios.post(`${API_BASE_URL}/ingest`);
};

const logService = {
    searchLogs,
    ingestLogs
};

export default logService;
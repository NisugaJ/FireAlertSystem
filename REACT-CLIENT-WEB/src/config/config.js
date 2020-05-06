import React from 'react';
import Axios from 'axios';

var Config = {
    apiBaseURL: "http://localhost:8082/fire_monitor/api"
}

var axios = require('axios');

var baseAxios = axios.create({
    baseURL: Config.apiBaseURL,
    timeout: 6000,
    headers: {
        // 'Host': 'localhost:8082',
        // 'Connection': 'keep-alive',
        'Access-Control-Allow-Origin': 'http://localhost:3000',
        // 'Access-Control-Allow-Methods': 'GET, HEAD, POST, OPTIONS',
        // 'Access-Control-Allow-Headers': 'Content-Type',
        'Content-Type': 'application/json'
    }
})
export { Config, baseAxios }
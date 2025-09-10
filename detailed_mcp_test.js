const { spawn } = require('child_process');

const env = {
    MYSQL_HOST: '172.20.144.1',
    MYSQL_PORT: '3306',
    MYSQL_USER: 'wsl_user',
    MYSQL_PASSWORD: '123456',
    MYSQL_DATABASE: 'study',
    DEBUG: '*',
    NODE_ENV: 'development'
};

console.log('环境变量:', env);
console.log('启动 MCP 服务器...');

const server = spawn('npx', ['@benborla29/mcp-server-mysql'], {
    env: { ...process.env, ...env },
    stdio: ['pipe', 'pipe', 'pipe']
});

let hasOutput = false;

server.stdout.on('data', (data) => {
    hasOutput = true;
    console.log('STDOUT:', data.toString());
});

server.stderr.on('data', (data) => {
    hasOutput = true;
    console.log('STDERR:', data.toString());
});

server.on('spawn', () => {
    console.log('进程已启动');
});

server.on('error', (err) => {
    console.log('启动错误:', err);
});

server.on('close', (code, signal) => {
    console.log(`进程退出 - 代码: ${code}, 信号: ${signal}`);
    if (!hasOutput) {
        console.log('警告: 进程没有产生任何输出');
    }
});

setTimeout(() => {
    if (server.pid) {
        console.log('强制终止进程...');
        server.kill('SIGTERM');
    }
}, 3000);

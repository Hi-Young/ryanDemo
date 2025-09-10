const { spawn } = require('child_process');

console.log('启动 MCP 服务器...');
const server = spawn('npx', ['@benborla29/mcp-server-mysql'], {
  env: {
    ...process.env,
    MYSQL_HOST: '172.20.144.1',
    MYSQL_PORT: '3306',
    MYSQL_USER: 'wsl_user',
    MYSQL_PASSWORD: '123456',
    MYSQL_DATABASE: 'study'
  },
  stdio: ['pipe', 'pipe', 'pipe']
});

server.stdout.on('data', (data) => {
  console.log('STDOUT:', data.toString());
});

server.stderr.on('data', (data) => {
  console.log('STDERR:', data.toString());
});

server.on('error', (err) => {
  console.log('进程错误:', err);
});

server.on('close', (code) => {
  console.log('进程退出，代码:', code);
});

// 发送初始化消息测试连接
setTimeout(() => {
  const initMessage = {
    jsonrpc: "2.0",
    id: 1,
    method: "initialize",
    params: {
      protocolVersion: "2024-11-05",
      capabilities: {},
      clientInfo: {
        name: "test-client",
        version: "1.0.0"
      }
    }
  };
  
  server.stdin.write(JSON.stringify(initMessage) + '\n');
}, 1000);

setTimeout(() => {
  server.kill();
}, 5000);

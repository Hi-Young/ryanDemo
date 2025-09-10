const { spawn } = require('child_process');

const server = spawn('npx', ['-y', '@f4ww4z/mcp-mysql-server'], {
  env: {
    ...process.env,
    MYSQL_HOST: '172.20.144.1',
    MYSQL_USER: 'wsl_user', 
    MYSQL_PASSWORD: '123456',
    MYSQL_DATABASE: 'study'
  },
  stdio: ['pipe', 'pipe', 'pipe']
});

server.stdout.on('data', (data) => {
  console.log('工具列表:', data.toString());
});

server.stderr.on('data', (data) => {
  console.log('错误:', data.toString());
});

setTimeout(() => {
  const initMsg = {
    jsonrpc: "2.0",
    id: 1,
    method: "initialize", 
    params: {
      protocolVersion: "2024-11-05",
      capabilities: {},
      clientInfo: {name: "test", version: "1.0.0"}
    }
  };
  server.stdin.write(JSON.stringify(initMsg) + '\n');
}, 500);

setTimeout(() => {
  const toolsMsg = {
    jsonrpc: "2.0",
    id: 2,
    method: "tools/list"
  };
  server.stdin.write(JSON.stringify(toolsMsg) + '\n');
}, 1000);

setTimeout(() => server.kill(), 2000);

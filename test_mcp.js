const { spawn } = require('child_process');

const server = spawn('npx', ['@benborla29/mcp-server-mysql'], {
  env: {
    ...process.env,
    MYSQL_HOST: '172.20.144.1',
    MYSQL_PORT: '3306',
    MYSQL_USER: 'wsl_user',
    MYSQL_PASSWORD: '123456',
    MYSQL_DATABASE: 'study'
  }
});

server.stdout.on('data', (data) => {
  console.log('STDOUT:', data.toString());
});

server.stderr.on('data', (data) => {
  console.log('STDERR:', data.toString());
});

setTimeout(() => {
  server.kill();
}, 5000);

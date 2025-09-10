const mysql = require('mysql2/promise'); // 使用 promise 版本更简单

async function main() {
  const sqlQuery = process.argv[2]; // 从命令行第二个参数获取SQL
  if (!sqlQuery) {
    console.error(JSON.stringify({ error: "No SQL query provided." }));
    return;
  }

  let connection;
  try {
    connection = await mysql.createConnection({
      host: '172.20.144.1',
      user: 'wsl_user',
      password: '123456',
      database: 'study'
    });

    const [rows, fields] = await connection.execute(sqlQuery);
    console.log(JSON.stringify(rows, null, 2)); // 以格式化的JSON输出结果

  } catch (err) {
    console.error(JSON.stringify({ error: err.message }));
  } finally {
    if (connection) connection.end();
  }
}

main();

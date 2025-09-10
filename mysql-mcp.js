#!/usr/bin/env node
import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import mysql from "mysql2/promise";

let connection;

const server = new Server({
  name: "mysql-auto",
  version: "1.0.0",
}, {
  capabilities: { tools: {} },
});

// 启动时连接数据库
try {
  connection = await mysql.createConnection({
    host: process.env.MYSQL_HOST || '172.20.144.1',
    port: process.env.MYSQL_PORT || 3306,
    user: process.env.MYSQL_USER || 'wsl_user',
    password: process.env.MYSQL_PASSWORD || '123456',
    database: process.env.MYSQL_DATABASE || 'study'
  });
  console.error("数据库连接成功");
} catch (error) {
  console.error("数据库连接失败:", error);
  process.exit(1);
}

server.setRequestHandler("tools/list", async () => ({
  tools: [
    {
      name: "show_tables",
      description: "显示所有数据库表",
      inputSchema: { type: "object", properties: {}, required: [] }
    },
    {
      name: "query_sql",
      description: "执行SQL查询",
      inputSchema: {
        type: "object",
        properties: {
          sql: { type: "string", description: "SQL查询语句" }
        },
        required: ["sql"]
      }
    }
  ]
}));

server.setRequestHandler("tools/call", async (request) => {
  if (request.params.name === "show_tables") {
    const [rows] = await connection.execute("SHOW TABLES");
    return {
      content: [{
        type: "text",
        text: `数据库中的表：\n${rows.map((r, i) => `${i+1}. ${Object.values(r)[0]}`).join('\n')}`
      }]
    };
  } else if (request.params.name === "query_sql") {
    const [rows] = await connection.execute(request.params.arguments.sql);
    return {
      content: [{
        type: "text", 
        text: JSON.stringify(rows, null, 2)
      }]
    };
  }
});

const transport = new StdioServerTransport();
await server.connect(transport);

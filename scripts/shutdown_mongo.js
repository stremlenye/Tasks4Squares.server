conn = new Mongo();
db = conn.getDB("admin");
db.shutdownServer();

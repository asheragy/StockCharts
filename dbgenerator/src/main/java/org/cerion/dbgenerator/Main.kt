package org.cerion.dbgenerator

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

// http://www.nasdaqtrader.com/trader.aspx?id=symboldirdefs
// ftp://ftp.nasdaqtrader.com/SymbolDirectory/otherlisted.txt

const val RootDirectory = "symboldata"
const val DatabaseName = "app\\src\\main\\assets\\symbols.db"
const val CreateDatabase = "CREATE TABLE symbols (symbol TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL DEFAULT '', exchange TEXT NOT NULL DEFAULT '')"

fun main() {

    val statement = "INSERT INTO symbols (symbol, name, exchange) VALUES(?, ?, ?)"
    val conn = getConnection()
    conn.autoCommit = false

    conn.prepareStatement(statement).run {
        var lines = readFile("$RootDirectory\\otherlisted.txt")
        println("Inserting Other symbols")
        for(i in 1 until lines.size - 1) {
            val line = lines[i].split("|")
            val exchange = when(line[2]) {
                "A" -> "NYSE MKT"
                "N" -> "NYSE"
                "P" -> "NYSE ARCA"
                "Z" -> "BATS"
                "V" -> "IEXG"
                else -> ""
            }

            insert(line[0], line[1], exchange)
        }
        executeBatch()
        conn.commit()

        lines = readFile("$RootDirectory\\nasdaqlisted.txt")
        println("Inserting NASDAQ")
        for(i in 1 until lines.size - 1) {
            val line = lines[i].split("|")
            insert(line[0], line[1], "NASDAQ")
        }

        executeBatch()
        conn.commit()

        println("Done")
    }
}

private fun Connection.insert(symbol: String, name: String, exchange: String) {

    val sql = "INSERT INTO symbols (symbol, name, exchange) VALUES(?, ?, ?)"
    try {
        val prepared = this.prepareStatement(sql)
        prepared.setString(1, symbol)
        prepared.setString(2, name)
        prepared.setString(3, exchange)
        prepared.executeUpdate()
    }
    catch(e: Exception) {
        e.printStackTrace()
    }
}

private fun PreparedStatement.insert(symbol: String, name: String, exchange: String) {
    try {
        setString(1, symbol)
        setString(2, name)
        setString(3, exchange)
        addBatch()
    }
    catch(e: Exception) {
        e.printStackTrace()
    }
}

private fun getConnection(): Connection {
    val file = File(DatabaseName)
    if (file.exists())
        file.delete()

    val conn = DriverManager.getConnection("jdbc:sqlite:$DatabaseName")
    conn.createStatement().execute(CreateDatabase)

    return conn
}

private fun readFile(filePath: String): List<String> {
    val lines = mutableListOf<String>()

    Files.lines(Paths.get(filePath), StandardCharsets.UTF_8).use { stream ->
        stream.forEach { s ->
            lines.add(s)
        }
    }

    return lines
}
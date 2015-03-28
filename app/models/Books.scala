package models

case class Book(title: String)

case class Books(introduction: String, books: Seq[Book])

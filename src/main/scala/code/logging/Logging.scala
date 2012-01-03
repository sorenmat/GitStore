package com.schantz.scala

trait Logging {

  def info(msg: String) {
    println("[INFO] " + new java.util.Date + " : " + msg)
  }

  def debug(msg: String) {
    println("[DEBUG] " + new java.util.Date + " : " + msg)
  }
  
  def error(msg: String) {
    println("[ERROR] " + new java.util.Date + " : " + msg)
  }
  
}
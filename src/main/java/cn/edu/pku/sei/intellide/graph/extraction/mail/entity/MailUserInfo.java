package cn.edu.pku.sei.intellide.graph.extraction.mail.entity;

/*
 * Email 用户（邮件发送者，邮件接收者）的信息：
 * 		用户昵称
 *  	邮件地址
 *  如："From: Scott Ganyo <scott.ganyo@eTapestry.com>"中用户昵称为"Scott Ganyo"，
 *  												        邮件地址为"scott.ganyo@eTapestry.com"
 */
class MailUserInfo {
    public String names;
    public String mail;
}

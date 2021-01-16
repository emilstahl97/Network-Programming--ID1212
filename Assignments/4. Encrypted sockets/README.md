# Encrypted sockets
Your task is to write a program that connects to your @kth.se account, lists the contents and then retrieves the first mail from it. You are not allowed to use JavaMail, but should instead do it "manually" according to the IMAP-protocol. You should also send one mail to yourself using the SMTP-protocol. The webmail has the following configuration:

* Settings for receiving e-mail (incoming)
* Server: webmail.kth.se
* Port: 993
* Protocol: SSL/TLS
* Authentication: Normal password
* Settings for sending e-mail (outgoing)
* Server: smtp.kth.se
* Port: 587
* Protocol: STARTTLS
* Authentication: Normal password

#### Note:

In the first case (IMAP with SSL/TLS) you start with an encrypted session and in the second case (SMTP with STARTTLS) you switch to encryption during the session.
Full documentation of IMAP can be found in rfc3501, but to solve the task it will be sufficient to compare with an IMAP-session for example the one here:

https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol

Usefull examples of SMTP-sessions can be found here:

https://www.samlogic.net/articles/smtp-commands-reference.htm 

You do not need a certificate (you are acting client)

#### Extra assignment: 

Change your browser in Task2 to use encryption and verify that a commercial (not your own hack) browser can connect and play the game. Since it's encryption on the server you are setting up, you need to change the game to use encryption with a certificate.
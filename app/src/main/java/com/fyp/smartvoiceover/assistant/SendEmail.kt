package com.fyp.smartvoiceover.assistant

import android.os.AsyncTask
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.security.AccessController
import java.security.PrivilegedAction
import java.security.Provider
import java.security.Security
import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.*
import kotlin.jvm.Synchronized
import kotlin.Throws
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress


 class SendEmailTask(AsyncResponse: AsyncResponse?) : AsyncTask<String, Void?, Void?>() {

     private var listener: AsyncResponse? = null

     init {
         listener = AsyncResponse
     }

     override fun onPreExecute() {
        super.onPreExecute()
        Log.i("Email sending", "sending start")
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
    }

     override fun doInBackground(vararg params: String?): Void? {
         try {
             val email:String = params[0]!!
             val password:String = params[1]!!
             val sub:String = params[2]!!
             val body:String = params[3]!!
             val recepient:String = params[4]!!

             val sender = GmailSender(email, password)
             //subject, body, sender, to
             sender.sendMail(
                 sub,
                 body,
                 email,
                 recepient
             )
             Log.i("Email sending", "send Done")
             listener!!.onSuccess()
         } catch (e: Exception) {
             Log.i("Email sending", "cannot send")
             listener!!.onFailure()
             e.printStackTrace()
         }
         return null
     }
 }


internal class JSSEProvider : Provider("HarmonyJSSE", 1.0, "Harmony JSSE Provider") {
    init {
        AccessController.doPrivileged(PrivilegedAction<Void?> {
            put(
                "SSLContext.TLS",
                "org.apache.harmony.xnet.provider.jsse.SSLContextImpl"
            )
            put("Alg.Alias.SSLContext.TLSv1", "TLS")
            put(
                "KeyManagerFactory.X509",
                "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl"
            )
            put(
                "TrustManagerFactory.X509",
                "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl"
            )
            null
        })
    }
}

class GmailSender(private val user: String, private val password: String) :
    Authenticator() {
    private val session: Session

    companion object {
        init {
            Security.addProvider(JSSEProvider())
        }
    }

    override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(user, password)
    }

    @Synchronized
    @Throws(Exception::class)
    fun sendMail(subject: String?, body: String, sender: String?, recipients: String) {
        try {
            val message = MimeMessage(session)
            val handler: DataHandler =
                DataHandler(ByteArrayDataSource(body.toByteArray(), "text/plain"))
            message.sender = InternetAddress(sender)
            message.subject = subject
            message.dataHandler = handler
            if (recipients.indexOf(',') > 0) message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipients)
            ) else message.setRecipient(
                Message.RecipientType.TO, InternetAddress(recipients)
            )
            Transport.send(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class ByteArrayDataSource : DataSource {
        private var data: ByteArray
        private var type: String? = null

        constructor(data: ByteArray, type: String?) : super() {
            this.data = data
            this.type = type
        }

        constructor(data: ByteArray) : super() {
            this.data = data
        }

        fun setType(type: String?) {
            this.type = type
        }

        override fun getContentType(): String {
            return type ?: "application/octet-stream"
        }

        @Throws(IOException::class)
        override fun getInputStream(): InputStream {
            return ByteArrayInputStream(data)
        }

        override fun getName(): String {
            return "ByteArrayDataSource"
        }

        @Throws(IOException::class)
        override fun getOutputStream(): OutputStream {
            throw IOException("Not Supported")
        }
    }

    init {
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        props.setProperty("mail.smtp.host", "smtp.gmail.com")
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.fallback"] = "false"
        props.setProperty("mail.smtp.quitwait", "false")
        session = Session.getDefaultInstance(props, this)
    }
}
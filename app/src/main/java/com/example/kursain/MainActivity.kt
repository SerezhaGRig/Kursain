package com.example.kursain

import android.Manifest
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import java.net.URL

import java.net.HttpURLConnection
import org.json.JSONObject

import android.util.Log
import android.widget.EditText
import java.io.DataOutputStream
import java.net.URLEncoder

import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream

const val MY_PERMISSIONS_REQUEST_INTERNET: Int = 1
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permission()
        val login_plain = findViewById<EditText>(R.id.login)
        val pass_plain = findViewById<EditText>(R.id.pass)
        val result_view = findViewById<TextView>(R.id.result)
        val auth_button = findViewById<Button>(R.id.auth)
        val reg_button = findViewById<Button>(R.id.reg)
        // var id = 1
        auth_button.setOnClickListener {
            val auth = Authorization(
                login_plain.text.toString(),
                pass_plain.text.toString(),/*id,*/
                result_view
            )
            auth.execute(null)
        }
        reg_button.setOnClickListener {
            val reg = Registration(
                login_plain.text.toString(),
                pass_plain.text.toString(),/*id,*/
                result_view
            )
            reg.execute(null)
        }


    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_INTERNET -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_LONG).show()
                    // functionality that depends on this permission.
                }
                return
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            }
        }
    }
    private fun permission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Toast.makeText(this, "No Permission", Toast.LENGTH_LONG).show()
            // Should we show an explanation?
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.INTERNET
                )
            ) {
                // No explanation needed, we can request the permission.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.INTERNET),
                        MY_PERMISSIONS_REQUEST_INTERNET
                    )
                }

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            // Toast.makeText(this, "No Permissions", Toast.LENGTH_LONG).show()
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
        }
    }


    class Authorization(
        private val login_plain: String,
        private val pass_plain: String,/*var id:Int,*/
        private val result_view: TextView
    ) : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String?): String {
            val url = URL("http://stupid-octopus-39.serverless.social/authenticate")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.doInput = true
            conn.connect()

            val jsonParam = JSONObject()
            jsonParam.put("username", login_plain)
            jsonParam.put("password", pass_plain)
            //jsonParam.put("id",id )
            //id+=1


            val os = DataOutputStream(conn.outputStream)
            os.writeBytes(jsonParam.toString())

            os.flush()
            os.close()
            val input = conn.inputStream
            //if (conn.responseCode.equals( HttpURLConnection.HTTP_OK)){
            // }

            val out = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            val bytesRead:Int = input.read(buffer)
            while ((bytesRead) > 0) {
                out.write(buffer, 0, bytesRead)
            }
            Log.i("STATUS", conn.responseCode.toString())
            Log.i("MSG", conn.responseMessage)
            val resultJson = String(out.toByteArray())
            out.close()
            conn.disconnect()
            secondRequest(resultJson)
            return resultJson
        }

        private fun secondRequest(result: String) {
            val token = JSONObject(result).getString("jwt")
            val url = URL("http://stupid-octopus-39.serverless.social/hello")
            val conn = url.openConnection() as HttpURLConnection
            conn.addRequestProperty("Authorization", "Bearer $token")
            val out = ByteArrayOutputStream()
            val input = conn.inputStream


            val buffer = ByteArray(1024)
            val bytesRead = input.read(buffer)
            while ((bytesRead) > 0) {
                out.write(buffer, 0, bytesRead)
            }
            val resultJson = String(out.toByteArray())
            out.close()
            Handler(Looper.getMainLooper()).post {
                result_view.text = JSONObject(resultJson).getString("data")
            }
            result_view.text = JSONObject(resultJson).getString("data")

        }

    }


    class Registration(
        private val login_plain: String,
        private val pass_plain: String,/*var id:Int,*/
        private val result_view: TextView
    ) : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String?): String {
            val url = URL("http://stupid-octopus-39.serverless.social/authenticate")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            //conn.doInput = true
            //conn.connect()

            val jsonParam = JSONObject()
            jsonParam.put("username", login_plain)
            jsonParam.put("password", pass_plain)
            //jsonParam.put("id",id )
            //id+=1


            val os = DataOutputStream(conn.outputStream)
            os.writeBytes(jsonParam.toString())

            os.flush()
            os.close()

            conn.disconnect()

            return "ok"
        }


    }
}


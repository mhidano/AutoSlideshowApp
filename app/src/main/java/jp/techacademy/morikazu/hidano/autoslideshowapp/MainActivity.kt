package jp.techacademy.morikazu.hidano.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private val STATE_STAY = 0
    private val STATE_NEXT = 1
    private val STATE_PREV = 2
    private val STATE_PLAY = 3

    private var index_count: Int = 0
    private var state = STATE_STAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //【パーミッションの許可状態確認】
        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        next_button.setOnClickListener {
            state = STATE_NEXT
            index_count++
            getContentsInfo()
            Log.d("ANDROID", "進むボタンが押された")
        }

        prev_button.setOnClickListener {
            state = STATE_PREV
            index_count--
            getContentsInfo()
            Log.d("ANDROID", "戻るボタンが押された")
        }

        play_button.setOnClickListener {
            state = STATE_PLAY
            getContentsInfo()
            Log.d("ANDROID", "再生/停止ボタンが押された")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            val index_max = cursor.count
            if (index_max <= index_count) {
                index_count = 0
            } else if (index_count < 0) {
                index_count = index_max - 1
            }

            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex) + index_count
            val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)

            Log.d("ANDROID", "index_max : " + index_max)
            Log.d("ANDROID", "fieldIndex : " + fieldIndex)
            Log.d("ANDROID", "id : " + id)
            Log.d("ANDROID", "URI : " + imageUri.toString())

            when (state) {
                STATE_NEXT -> {
                    cursor.moveToNext()
                }
                STATE_PREV -> {
                    cursor.moveToPrevious()
                }
            }
        }
        cursor.close()
    }
}
package com.eje_c.vsaplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import org.joml.Quaternionf
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var player: VSAPlayer
    private var seeking: Boolean = false
    private val headOrientation = Quaternionf()
    private var sensorValueOffset: Float = -1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        player = VSAPlayer(this)

        // 再生終了後に停止状態にする
        player.onPlayEnd = this@MainActivity::pause

        // ツールバーメニューをタップでドロワーを開く
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }

        // ナビゲーションメニュー
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_openFile -> {
                    performFileSearch()
                    true
                }
                else -> false
            }
        }

        // 再生・一時停止ボタン
        playPauseButton.setOnClickListener {
            if (player.isPlaying) {
                pause()
            } else {
                play()
            }
        }

        // シークバー操作
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.currentTimeInMillis = progress.toLong()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                seeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seeking = false
            }

        })

        // 回転方向を反映
        rotatorView.onAngleChanged = { angle ->

            // Degrees to radians
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()

            // Create quaternion
            headOrientation.identity()
                .rotateY(angleRad)
                .invert()

            // Update player
            player.updateOrientation(headOrientation.x, headOrientation.y, headOrientation.z, headOrientation.w)
        }

        useDeviceSensorCheck.setOnCheckedChangeListener { _, isChecked ->

            // タッチコントロールの切り替え
            rotatorView.touchControlEnabled = !isChecked

            // センサーを使用
            if (isChecked) {
                registerSensorListener()
            } else {
                unregisterSensorListener()
            }
        }

        // 初期表示状態
        drawerLayout.openDrawer(Gravity.LEFT)
        playPauseButton.isEnabled = false
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        if (useDeviceSensorCheck.isChecked) {
            registerSensorListener()
        }
    }

    private fun registerSensorListener() {
        sensorValueOffset = -1.0f

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        unregisterSensorListener()
        super.onPause()
    }

    private fun unregisterSensorListener() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (sensorValueOffset < 0.0f) {
            sensorValueOffset = event.values[0]
        } else {
            val value = event.values[0] - sensorValueOffset
            if (value < 0.0f) {
                rotatorView.angle = value + 360
            } else {
                rotatorView.angle = value
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            READ_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    onPickFile(data.data)
                }
            }
        }
    }

    /**
     * ファイルを開く
     */
    private fun performFileSearch() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType("*/*")

        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    /**
     * ファイルを開く処理が成功したときのコールバック
     */
    private fun onPickFile(uri: Uri) {

        // 読み込む
        player.prepare(uri)

        // 再生停止ボタンを有効化
        playPauseButton.isEnabled = true

        // 再生
        play()

        // ドロワーを閉じる
        drawerLayout.closeDrawers()
    }

    /**
     * 再生開始する。
     */
    private fun play() {
        player.play()
        playPauseButton.setImageResource(R.drawable.ic_pause)

        // 再生中は一定間隔でUIを更新
        thread {
            while (player.isPlaying) {
                runOnUiThread(this@MainActivity::updateSeekbar)
                Thread.sleep(100)
            }
        }
    }

    /**
     * 一時停止する。
     */
    private fun pause() {
        player.pause()
        player.currentTimeInMillis = 0
        playPauseButton.setImageResource(R.drawable.ic_play)
    }

    private fun updateSeekbar() {

        // 時間表示を更新
        val currentTimeInMillis = player.currentTimeInMillis
        val durationInMillis = player.duration
        val currentTime = currentTimeInMillis.millisToTimeString()
        val duration = durationInMillis.millisToTimeString()
        time.text = "$currentTime / $duration"

        // 手動シーク中以外は、シークバーを更新
        if (!seeking) {
            seekBar.max = durationInMillis.toInt()
            seekBar.progress = currentTimeInMillis.toInt()
        }

    }

    companion object {
        private val READ_REQUEST_CODE = 42
    }
}

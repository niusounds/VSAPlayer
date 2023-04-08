package com.eje_c.vsaplayer

import android.content.Context
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.eje_c.vsaplayer.databinding.MainActivityBinding
import com.niusounds.vsaplayer.orientationFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.joml.Quaternionf
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val pickFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            if (result != null) {
                onPickFile(result)
            }
        }
    private lateinit var binding: MainActivityBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var player: VSAPlayer
    private var seeking: Boolean = false
    private val headOrientation = Quaternionf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        player = VSAPlayer(this)

        // 再生終了後に停止状態にする
        player.onPlayEnd = this@MainActivity::pause

        // ツールバーメニューをタップでドロワーを開く
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.LEFT)
        }

        // ナビゲーションメニュー
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_openFile -> {
                    performFileSearch()
                    true
                }

                else -> false
            }
        }

        // 再生・一時停止ボタン
        binding.playPauseButton.setOnClickListener {
            if (player.isPlaying) {
                pause()
            } else {
                play()
            }
        }

        // シークバー操作
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
        binding.rotatorView.onAngleChanged = { angle ->

            // Degrees to radians
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()

            // Create quaternion
            headOrientation.identity()
                .rotateY(angleRad)
                .invert()

            // Update player
            player.updateOrientation(
                headOrientation.x,
                headOrientation.y,
                headOrientation.z,
                headOrientation.w
            )
        }

        binding.useDeviceSensorCheck.setOnCheckedChangeListener { _, isChecked ->

            // タッチコントロールの切り替え
            binding.rotatorView.touchControlEnabled = !isChecked

            // センサーを使用
            if (isChecked) {
                registerSensorListener()
            } else {
                unregisterSensorListener()
            }
        }

        // 初期表示状態
        binding.drawerLayout.openDrawer(Gravity.LEFT)
        binding.playPauseButton.isEnabled = false
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        if (binding.useDeviceSensorCheck.isChecked) {
            registerSensorListener()
        }
    }

    private var sensorCollectJob: Job? = null

    private fun registerSensorListener() {
        var sensorValueOffset = -1.0f

        sensorCollectJob = lifecycleScope.launch {
            sensorManager.orientationFlow().collect { event ->
                if (sensorValueOffset < 0.0f) {
                    sensorValueOffset = event.values[0]
                } else {
                    val value = event.values[0] - sensorValueOffset
                    if (value < 0.0f) {
                        binding.rotatorView.angle = value + 360
                    } else {
                        binding.rotatorView.angle = value
                    }
                }
            }
        }
    }

    override fun onPause() {
        unregisterSensorListener()
        super.onPause()
    }

    private fun unregisterSensorListener() {
        sensorCollectJob?.cancel()
        sensorCollectJob = null
    }

    /**
     * ファイルを開く
     */
    private fun performFileSearch() {
        pickFile.launch("*/*")
    }

    /**
     * ファイルを開く処理が成功したときのコールバック
     */
    private fun onPickFile(uri: Uri) {

        // 読み込む
        player.prepare(uri)

        // 再生停止ボタンを有効化
        binding.playPauseButton.isEnabled = true

        // 再生
        play()

        // ドロワーを閉じる
        binding.drawerLayout.closeDrawers()
    }

    /**
     * 再生開始する。
     */
    private fun play() {
        player.play()
        binding.playPauseButton.setImageResource(R.drawable.ic_pause)

        // 再生中は一定間隔でUIを更新
        thread {
            while (true) {
                runOnUiThread {
                    if (player.isPlaying) {
                        updateSeekbar()
                    }
                }
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
        binding.playPauseButton.setImageResource(R.drawable.ic_play)
    }

    private fun updateSeekbar() {

        // 時間表示を更新
        val currentTimeInMillis = player.currentTimeInMillis
        val durationInMillis = player.duration
        val currentTime = currentTimeInMillis.millisToTimeString()
        val duration = durationInMillis.millisToTimeString()
        binding.time.text = "$currentTime / $duration"

        // 手動シーク中以外は、シークバーを更新
        if (!seeking) {
            binding.seekBar.max = durationInMillis.toInt()
            binding.seekBar.progress = currentTimeInMillis.toInt()
        }

    }
}

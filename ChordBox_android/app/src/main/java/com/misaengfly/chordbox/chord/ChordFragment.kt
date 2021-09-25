package com.misaengfly.chordbox.chord

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.*
import android.media.AudioTrack
import android.media.MediaCodec.CodecException
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.misaengfly.chordbox.PlayService
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.FragmentChordBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt


class ChordFragment : Fragment() {

    companion object {
        private const val SEEK_OVER_AMOUNT = 5000
    }

    private lateinit var chordBinding: FragmentChordBinding

    private lateinit var playService: PlayService
    private var bound: Boolean = false

    /** service binding을 위한 callbacks 정의하고 bindService()로 전달 */
    private val connection = object : ServiceConnection {
        // Called when the connection with the service is established
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PlayService.PlayBinder
            playService = binder.getService()
            bound = true
        }

        // Called when the connection with the service disconnects unexpectedly
        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    private var isPlaying: Boolean = false
    private var isFirstPlay: Boolean = false

    private fun togglePlay() {
        if (!isFirstPlay && !isPlaying) {
            playService.startPlaying()
            chordBinding.musicPlayBtn.setImageResource(R.drawable.ic_pause)
            isFirstPlay = true
            isPlaying = true
        } else if (isFirstPlay && !isPlaying) {
            playService.resumePlaying()
            chordBinding.musicPlayBtn.setImageResource(R.drawable.ic_pause)
            isPlaying = true
        } else if (isPlaying) {
            playService.pausePlaying()
            chordBinding.musicPlayBtn.setImageResource(R.drawable.ic_play)
            isPlaying = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chordBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_chord,
            container,
            false
        )

        chordBinding.musicPlayBtn.setOnClickListener {
            //playService.startPlaying()
//            togglePlay()
        }
//
//        chordBinding.musicFastForwardBtn.setOnClickListener {
//            playService.stopPlaying()
//        }

        return chordBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //initUI()

        // connectService
        Intent(requireActivity(), PlayService::class.java).also { intent: Intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        initMusic()
        getDecode()

        chordBinding.musicPlayBtn.setOnClickListener {
            chordBinding.playerVisualizer.setWaveForm(decodeAmps, tickDuration)
        }
//        initUI()
    }

    private var channelCount: Int = 0
    private var bufferSize = 0
    private var bitPerSample: Int = 16
    private var sampleRate: Int = 44100
    private var byteRate: Long = 0
    private var tickDuration: Int = 0

    private fun initMusic() {
        val mid = 0
        val filePath = "${requireContext().filesDir?.absolutePath}/musicrecord${mid}.m4a"

        val extractor = MediaExtractor()
        extractor.setDataSource(filePath)

        val format: MediaFormat = extractor.getTrackFormat(0)
        channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        bitPerSample = when (format.getInteger("bits-per-sample")) {
            8 -> AudioFormat.ENCODING_PCM_8BIT
            16 -> AudioFormat.ENCODING_PCM_16BIT
            else -> AudioFormat.ENCODING_PCM_16BIT
        }
        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE) * 2

        byteRate = (bitPerSample * sampleRate * channelCount / 8).toLong()

        bufferSize = AudioRecord.getMinBufferSize(
            sampleRate, channelCount, bitPerSample
        )

        tickDuration = (format.getLong(MediaFormat.KEY_DURATION) / 1000 / 1000).toInt()
//        tickDuration = (bufferSize.toDouble() * 1000 / byteRate).toInt()
    }

    /**
     * Play Service와 Custom View 연결
     **/
    private fun initUI() = with(chordBinding) {
        playerVisualizer.apply {
            ampNormalizer = { sqrt(it.toFloat()).toInt() }
            onStartSeeking = {
//                player.pause()
                //togglePlay()
            }
//            onSeeking = { binding.timelineTextView.text = it.formatAsTime() }
//            onFinishedSeeking = { time, isPlayingBefore ->
//                player.seekTo(time)
//                if (isPlayingBefore) {
//                    player.resume()
//                }
//            }
//            onAnimateToPositionFinished = { time, isPlaying ->
//                updateTime(time, isPlaying)
//                player.seekTo(time)
//            }
        }
        musicPlayBtn.setOnClickListener {
//            player.togglePlay()
//            togglePlay()
        }
        musicForwardBtn.setOnClickListener { playerVisualizer.seekOver(SEEK_OVER_AMOUNT) }
        musicBackwardBtn.setOnClickListener { playerVisualizer.seekOver(-SEEK_OVER_AMOUNT) }

        lifecycleScope.launchWhenCreated {
            val amps = loadAmps()
            playerVisualizer.setWaveForm(amps, tickDuration)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadAmps(): List<Int> = withContext(IO) {
        val amps = mutableListOf<Int>()
        val buffer = ByteArray(bufferSize)

        File("${requireContext().filesDir?.absolutePath}/musicrecord0.m4a").inputStream().use {
            it.skip(44.toLong())

            var count = it.read(buffer)
            while (count > 0) {
                amps.add(buffer.calculateAmplitude())
                count = it.read(buffer)
            }
        }

        amps
    }

    private fun ByteArray.calculateAmplitude(): Int {
        return ShortArray(size / 2).let {
            ByteBuffer.wrap(this)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .get(it)
            it.maxOrNull()?.toInt() ?: 0
        }
    }

    private fun ByteArray.getInt(): Int {
        this.let {
            if (this.size < 4) return 0
            var result = this[3].toInt() and 0xFF
            result = result or (this[2].toInt() shl 8 and 0xFF00)
            result = result or (this[1].toInt() shl 16 and 0xFF0000)
            result = result or (this[0].toInt() shl 24)
            return result
        }
        return 0
    }

    // TODO()
    val decodeAmps = mutableListOf<Int>()
    fun getDecode() {
        val filePath = "${requireContext().filesDir?.absolutePath}/musicrecord0.m4a"

        // inizialize the mediaExtractor and set the source file
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(filePath)

        // select the first audio track in the file and return it's format
        var mediaFormat: MediaFormat? = null
        val numTracks: Int = mediaExtractor.trackCount
        for (i in 0 until numTracks) {
            mediaFormat = mediaExtractor.getTrackFormat(i)
            if (mediaFormat.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                mediaExtractor.selectTrack(i)
                break
            }
        }

        // we get the parameter from the mediaFormat
        channelCount = mediaFormat!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        sampleRate = mediaFormat!!.getInteger(MediaFormat.KEY_SAMPLE_RATE) * 2
        val mimeType = mediaFormat!!.getString(MediaFormat.KEY_MIME)

        // we can get the minimum buffer size from audioTrack passing the parameter of the audio
        // to keep it safe it's good practice to create a buffer that is 8 times bigger
        val minBuffSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // to reproduce the data we need to initialize the audioTrack, by passing the audio parameter
        // we use the MODE_STREAM so we can put more data dynamically with audioTrack.write()
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioSessionId = audioManager.generateAudioSessionId()

//        var audioTrack = AudioTrack(
//            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
//                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
//            AudioFormat.Builder().setSampleRate(sampleRate)
//                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//                .build(),
//            minBuffSize * 8,
//            AudioTrack.MODE_STREAM,
//            audioSessionId
//        )

        var audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build(),
            minBuffSize * 8,
            AudioTrack.MODE_STATIC,
            audioSessionId
        )

        // we get the mediaCodec by creating it using the mime_type extracted form the track
        val decoder = MediaCodec.createDecoderByType(mimeType!!)

        // to decode the file in asynchronous mode we set the callbacks
        decoder.setCallback(object : MediaCodec.Callback() {
            private var mOutputEOS = false
            private var mInputEOS = false
            override fun onInputBufferAvailable(
                codec: MediaCodec,
                index: Int
            ) {
                // if i reached the EOS i either the input or the output file i just skip
                if (mOutputEOS or mInputEOS) return

                // i must use the index to get the right ByteBuffer from the codec
                val inputBuffer = codec.getInputBuffer(index) ?: return

                // if the codec is null i just skip and wait for another buffer
                var sampleTime: Long = 0

                // with this method i fill the inputBuffer with the data read from the mediaExtractor
                val result: Int = mediaExtractor.readSampleData(inputBuffer, 0)
                // the return parameter of readSampleData is the number of byte read from the file
                // and if it's -1 it means that i reached EOS
                if (result >= 0) {
                    // if i read some bytes i can pass the index of the buffer, the number of bytes
                    // that are in the buffer and the sampleTime to the codec, so that it can decode
                    // that data
                    sampleTime = mediaExtractor.sampleTime
                    codec.queueInputBuffer(index, 0, result, sampleTime, 0)
                    mediaExtractor.advance()
                } else {
                    // if i reached EOS i need to tell the codec
                    codec.queueInputBuffer(index, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    mInputEOS = true
                }
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                // i can get the outputBuffer from the codec using the relative index
                val outputBuffer = codec.getOutputBuffer(index)

                // if i got a non null buffer
                if (outputBuffer != null) {
                    outputBuffer.rewind()
                    outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

                    val tempBuf = outputBuffer.duplicate()
                    val byteArray = ByteArray(tempBuf.limit())
                    tempBuf.get(byteArray)
                    decodeAmps.add(byteArray.calculateAmplitude())

                    // i just need to write the outputBuffer into the audioTrack passing the number of
                    // bytes it contain and using the WRITE_BLOCKING so that this call will block
                    // until it doesn't finish to write the data
                    val ret = audioTrack.write(
                        outputBuffer,
                        outputBuffer.remaining(),
                        AudioTrack.WRITE_BLOCKING
                    )
                }

                // if the flags in the MediaCodec.BufferInfo contains the BUFFER_FLAG_END_OF_STREAM
                // it mean that i reached EOS so i set mOutputEOS to true, and to assure
                // that it remain true even if this callback is called again i use the logical or
                mOutputEOS =
                    mOutputEOS or (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0)

                // i always need to release the buffer i use so the system can recycle them and use
                // it again
                codec.releaseOutputBuffer(index, false)

                // if i reached the end of the output stream i need to stop and release the codec
                // and the extractor
                if (mOutputEOS) {
                    codec.stop()
                    codec.release()
                    mediaExtractor.release()
                    audioTrack.release()
                }
            }

            override fun onError(
                codec: MediaCodec,
                e: CodecException
            ) {
                Timber.e(e, "mediacodec collback onError: %s", e.message)
            }

            override fun onOutputFormatChanged(
                codec: MediaCodec,
                format: MediaFormat
            ) {
                Timber.d("onOutputFormatChanged: %s", format.toString())
            }
        })

        // now we can configure the codec by passing the mediaFormat and start it
        decoder.configure(mediaFormat, null, null, 0)
        decoder.start()
        // also we need to start the audioTrack.
        val byteBuffer = ByteArray(tickDuration * sampleRate)
        audioTrack.write(byteBuffer, 0, byteBuffer.size)
        //audioTrack.play()
    }
}
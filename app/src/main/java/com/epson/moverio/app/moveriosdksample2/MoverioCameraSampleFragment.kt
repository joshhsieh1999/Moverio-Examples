package com.epson.moverio.app.moveriosdksample2

import android.R
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import com.epson.moverio.hardware.camera.CameraDevice
import com.epson.moverio.hardware.camera.CameraManager
import com.epson.moverio.hardware.camera.CameraProperty
import com.epson.moverio.hardware.camera.CaptureDataCallback
import com.epson.moverio.hardware.camera.CaptureDataCallback2
import com.epson.moverio.hardware.camera.CaptureStateCallback2
import com.epson.moverio.util.PermissionGrantResultCallback
import com.epson.moverio.util.PermissionHelper
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date

class MoverioCameraSampleFragment : Fragment(),
    CaptureStateCallback2, CaptureDataCallback, CaptureDataCallback2,
    PermissionGrantResultCallback {
    private val TAG = this.javaClass.simpleName
    private var mContext: Context? = null
    private var mCameraManager: CameraManager? = null
    private var mCameraDevice: CameraDevice? = null
    private val mCaptureStateCallback2: CaptureStateCallback2 = this
    private val mCaptureDataCallback: CaptureDataCallback = this
    private val mCaptureDataCallback2: CaptureDataCallback2 = this
    private var mToggleButton_cameraOpenClose: ToggleButton? = null
    private var mToggleButton_captureStartStop: ToggleButton? = null
    private var mToggleButton_previewStartStop: ToggleButton? = null
    private var mCheckBox_stateCallback: CheckBox? = null
    private var mCheckBox_dataCallback: CheckBox? = null
    private var mCheckBox_preview: CheckBox? = null
    private var mButton_takePicture: Button? = null
    private var mToggleButton_recording: ToggleButton? = null
    private var mSwitch_autoExposure: Switch? = null
    private var mTextView_exposure: TextView? = null
    private var mSeekBar_exposure: SeekBar? = null
    private var mTextView_brightness: TextView? = null
    private var mSeekBar_brightness: SeekBar? = null
    private var mTextView_whitebalance: TextView? = null
    private var mRadioGroup_whitebalance: RadioGroup? = null
    private var mTextView_powerLineFrequency: TextView? = null
    private var mRadioGroup_powerLineFrequency: RadioGroup? = null
    private var mTextView_captureFormat: TextView? = null
    private var mRadioGroup_captureFormat: RadioGroup? = null
    private var mTextView_indicatorMode: TextView? = null
    private var mRadioGroup_indicatorMode: RadioGroup? = null
    private var mSurfaceView_preview: SurfaceView? = null
    private var mTextView_captureState: TextView? = null
    private var mSpinner_captureInfo: Spinner? = null
    private var mSwitch_autoFocus: Switch? = null
    private var mTextView_focus: TextView? = null
    private var mSeekBar_focus: SeekBar? = null
    private var mTextView_cameraGain: TextView? = null
    private var mSeekBar_cameraGain: SeekBar? = null
    private var mTextView_framerate: TextView? = null
    private var mCalcurationRate_framerate: CalcurationRate? = null
    private var mTextView_test: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mContext = context
        mCameraManager = CameraManager(mContext, this)
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mToggleButton_cameraOpenClose =
            view.findViewById<View>(R.id.toggleButton_cameraOpenClose) as ToggleButton
        mToggleButton_cameraOpenClose!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                try {
                    mCameraDevice = mCameraManager!!.open(
                        if (mCheckBox_stateCallback!!.isChecked) mCaptureStateCallback2 else null,
                        if (mCheckBox_dataCallback!!.isChecked) mCaptureDataCallback else null,
                        if (mCheckBox_preview!!.isChecked) mSurfaceView_preview!!.holder else null
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                mCameraManager!!.close(mCameraDevice)
                mCameraDevice = null
            }
        }
        mToggleButton_captureStartStop =
            view.findViewById<View>(R.id.toggleButton_captureStartStop) as ToggleButton
        mToggleButton_captureStartStop!!.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                mTextView_test.setText(cameraProperty)
                if (isChecked) {
                    mCameraDevice!!.startCapture()
                } else {
                    mCameraDevice!!.stopCapture()
                }
            }
        })
        mToggleButton_previewStartStop =
            view.findViewById<View>(R.id.toggleButton_previewStartStop) as ToggleButton
        mToggleButton_previewStartStop!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mCameraDevice!!.startPreview()
            } else {
                mCameraDevice!!.stopPreview()
            }
        }
        mCheckBox_stateCallback = view.findViewById<View>(R.id.checkBox_stateCallback) as CheckBox
        mCheckBox_dataCallback = view.findViewById<View>(R.id.checkBox_dataCallback) as CheckBox
        mCheckBox_preview = view.findViewById<View>(R.id.checkBox_preview) as CheckBox
        mTextView_captureState = view.findViewById<View>(R.id.textView_captureState) as TextView
        mSpinner_captureInfo = view.findViewById<View>(R.id.spinner_cpatureInfo) as Spinner
        mSpinner_captureInfo!!.setOnTouchListener { view, motionEvent ->
            if (mCameraDevice != null) mSpinner_captureInfo!!.adapter =
                CaptureInfoAdapter(
                    mContext,
                    R.layout.simple_spinner_item,
                    mCameraDevice!!.property.supportedCaptureInfo
                )
            false
        }
        mSpinner_captureInfo!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val item = parent.selectedItem as IntArray
                    val property = mCameraDevice!!.property
                    property.setCaptureSize(item[0], item[1])
                    property.captureFps = item[2]
                    mCameraDevice!!.property = property
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        mButton_takePicture = view.findViewById<View>(R.id.button_takePicture) as Button
        mButton_takePicture!!.setOnClickListener {
            val fileName = "image_" + SimpleDateFormat("yyyyMMddHHmmss")
                .format(Date(System.currentTimeMillis())) + ".jpg"
            mCameraDevice!!.takePicture(
                File(
                    mContext!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    fileName
                )
            )
        }
        mToggleButton_recording =
            view.findViewById<View>(R.id.toggleButton_recording) as ToggleButton
        mToggleButton_recording!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val fileName = "movie_" + SimpleDateFormat("yyyyMMddHHmmss")
                    .format(Date(System.currentTimeMillis())) + ".mp4"
                mCameraDevice!!.startRecord(
                    File(
                        mContext!!.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                        fileName
                    )
                )
            } else {
                mCameraDevice!!.stopRecord()
            }
        }
        mSwitch_autoExposure = view.findViewById<View>(R.id.switch_autoExposure) as Switch
        mSwitch_autoExposure!!.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                val property = mCameraDevice!!.property
                if (isChecked) {
                    property.exposureMode = CameraProperty.EXPOSURE_MODE_AUTO
                } else {
                    property.exposureMode = CameraProperty.EXPOSURE_MODE_MANUAL
                }
                mCameraDevice!!.property = property
                mTextView_test.setText(cameraProperty)
            }
        })
        mTextView_exposure = view.findViewById<View>(R.id.textView_exposure) as TextView
        mSeekBar_exposure = view.findViewById<View>(R.id.seekBar_exposure) as SeekBar
        mSeekBar_exposure!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val property = mCameraDevice!!.property
                property.exposureStep = progress + property.exposureStepMin
                mCameraDevice!!.property = property
                mTextView_exposure!!.text = "Exposure:" + (progress + property.exposureStepMin)
                mTextView_test.setText(cameraProperty)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        mTextView_brightness = view.findViewById<View>(R.id.textView_brightness) as TextView
        mSeekBar_brightness = view.findViewById<View>(R.id.seekBar_brightness) as SeekBar
        mSeekBar_brightness!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val property = mCameraDevice!!.property
                property.brightness = progress + property.brightnessMin
                val ret = mCameraDevice!!.setProperty(property)
                mTextView_brightness!!.text = "Brightness:" + (progress + property.brightnessMin)
                mTextView_test.setText(cameraProperty)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        mTextView_whitebalance = view.findViewById<View>(R.id.textView_whitebalance) as TextView
        mRadioGroup_whitebalance =
            view.findViewById<View>(R.id.radioGroup_whitebalance) as RadioGroup
        mRadioGroup_whitebalance!!.setOnCheckedChangeListener(object :
            RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                val property = mCameraDevice!!.property
                when (checkedId) {
                    R.id.radioButton_auto -> property.whiteBalanceMode =
                        CameraProperty.WHITE_BALANCE_MODE_AUTO

                    R.id.radioButton_cloudyDaylight -> property.whiteBalanceMode =
                        CameraProperty.WHITE_BALANCE_MODE_CLOUDY_DAYLIGHT

                    R.id.radioButton_daylight -> property.whiteBalanceMode =
                        CameraProperty.WHITE_BALANCE_MODE_DAYLIGHT

                    R.id.radioButton_fluorescent -> property.whiteBalanceMode =
                        CameraProperty.WHITE_BALANCE_MODE_FLUORESCENT

                    R.id.radioButton_incandescent -> property.whiteBalanceMode =
                        CameraProperty.WHITE_BALANCE_MODE_INCANDESCENT

                    R.id.radioButton_twilight -> property.whiteBalanceMode =
                        CameraProperty.WHITE_BALANCE_MODE_TWILIGHT

                    else -> Log.w(TAG, "id=$checkedId")
                }
                mCameraDevice!!.property = property
                mTextView_whitebalance!!.text = "White balance:" + property.whiteBalanceMode
                mTextView_test.setText(cameraProperty)
            }
        })
        mTextView_powerLineFrequency =
            view.findViewById<View>(R.id.textView_powerLineFrequency) as TextView
        mRadioGroup_powerLineFrequency =
            view.findViewById<View>(R.id.radioGroup_powerLineFrequency) as RadioGroup
        mRadioGroup_powerLineFrequency!!.setOnCheckedChangeListener(object :
            RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                val property = mCameraDevice!!.property
                when (checkedId) {
                    R.id.radioButton_50Hz -> property.powerLineFrequencyControlMode =
                        CameraProperty.POWER_LINE_FREQUENCY_CONTROL_MODE_50HZ

                    R.id.radioButton_60Hz -> property.powerLineFrequencyControlMode =
                        CameraProperty.POWER_LINE_FREQUENCY_CONTROL_MODE_60HZ

                    R.id.radioButton_disable -> property.powerLineFrequencyControlMode =
                        CameraProperty.POWER_LINE_FREQUENCY_CONTROL_MODE_DISABLE

                    else -> Log.w(TAG, "id=$checkedId")
                }
                mCameraDevice!!.property = property
                mTextView_powerLineFrequency!!.text =
                    "Power line frequency:" + property.powerLineFrequencyControlMode
                mTextView_test.setText(cameraProperty)
            }
        })

        // Capture format : deprecated
        mTextView_captureFormat = view.findViewById<View>(R.id.textView_captureFormat) as TextView
        mRadioGroup_captureFormat =
            view.findViewById<View>(R.id.radioGroup_captureFormat) as RadioGroup
        mRadioGroup_captureFormat!!.setOnCheckedChangeListener(object :
            RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                val property = mCameraDevice!!.property
                when (checkedId) {
                    R.id.radioButton_rgb565 -> property.captureDataFormat =
                        CameraProperty.CAPTURE_DATA_FORMAT_RGB_565

                    R.id.radioButton_argb8888 -> property.captureDataFormat =
                        CameraProperty.CAPTURE_DATA_FORMAT_ARGB_8888

                    R.id.radioButton_yuy2 -> property.captureDataFormat =
                        CameraProperty.CAPTURE_DATA_FORMAT_YUY2

                    R.id.radioButton_h264 -> property.captureDataFormat =
                        CameraProperty.CAPTURE_DATA_FORMAT_H264

                    else -> Log.w(TAG, "id=$checkedId")
                }

                // Change capture size & framerate.
                val list = property.supportedCaptureInfo
                property.setCaptureSize(list[0][0], list[0][1])
                property.captureFps = list[0][2]
                mCameraDevice!!.property = property
                mTextView_captureFormat!!.text = "Capture format:" + property.captureDataFormat
                mTextView_test.setText(cameraProperty)
            }
        })
        mTextView_indicatorMode = view.findViewById<View>(R.id.textView_indicatorMode) as TextView
        mRadioGroup_indicatorMode =
            view.findViewById<View>(R.id.radioGroup_indicatorMode) as RadioGroup
        mRadioGroup_indicatorMode!!.setOnCheckedChangeListener(object :
            RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                val property = mCameraDevice!!.property
                when (checkedId) {
                    R.id.radioButton_indicatorMode_auto -> property.indicatorMode =
                        CameraProperty.INDICATOR_MODE_AUTO

                    R.id.radioButton_indicatorMode_on -> property.indicatorMode =
                        CameraProperty.INDICATOR_MODE_ON

                    R.id.radioButton_indicatorMode_off -> property.indicatorMode =
                        CameraProperty.INDICATOR_MODE_OFF

                    else -> Log.w(TAG, "id=$checkedId")
                }
                mCameraDevice!!.property = property
                mTextView_indicatorMode!!.text = "Indicator mode:" + property.indicatorMode
                mTextView_test.setText(cameraProperty)
            }
        })
        mSwitch_autoFocus = view.findViewById<View>(R.id.switch_autoFocus) as Switch
        mSwitch_autoFocus!!.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                val property = mCameraDevice!!.property
                if (isChecked) {
                    property.focusMode = CameraProperty.FOCUS_MODE_AUTO
                } else {
                    property.focusMode = CameraProperty.FOCUS_MODE_MANUAL
                }
                mCameraDevice!!.property = property
                mTextView_test.setText(cameraProperty)
            }
        })
        mTextView_focus = view.findViewById<View>(R.id.textView_focus) as TextView
        mSeekBar_focus = view.findViewById<View>(R.id.seekBar_focus) as SeekBar
        mSeekBar_focus!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val property = mCameraDevice!!.property
                property.focusDistance = progress + property.focusDistanceMin
                val ret = mCameraDevice!!.setProperty(property)
                mTextView_focus!!.text =
                    "Focus:" + mCameraDevice!!.property.focusDistance + "(" + mCameraDevice!!.property.focusDistanceMin + " - " + mCameraDevice!!.property.focusDistanceMax + "), AF=" + mCameraDevice!!.property.focusMode + "(" + ret + ")"
                mTextView_test.setText(cameraProperty)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        mTextView_cameraGain = view.findViewById<View>(R.id.textView_cameraGain) as TextView
        mSeekBar_cameraGain = view.findViewById<View>(R.id.seekBar_cameraGain) as SeekBar
        mSeekBar_cameraGain!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val property = mCameraDevice!!.property
                property.gain = progress + property.gainMin
                val ret = mCameraDevice!!.setProperty(property)
                mTextView_cameraGain!!.text = "Gain:" + property.gain
                mTextView_test.setText(cameraProperty)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        mSurfaceView_preview = view.findViewById<View>(R.id.surfaceView_preview) as SurfaceView
        mTextView_framerate = view.findViewById<View>(R.id.textView_framerate) as TextView
        mCalcurationRate_framerate = CalcurationRate(mTextView_framerate)
        mCalcurationRate_framerate!!.start()
        mTextView_test = view.findViewById<View>(R.id.textView_test) as TextView
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCameraManager!!.release()
        mCameraManager = null
        mCalcurationRate_framerate!!.finish()
    }

    override fun onCaptureData(timestamp: Long, data: ByteArray) {
        mTextView_captureState!!.text = "onCaptureData:" + timestamp + ",size:" + data.size
        mCalcurationRate_framerate!!.updata()
    }

    override fun onCameraOpened() {
        Log.d(TAG, "onCameraOpened")
        mTextView_captureState!!.text = "onCameraOpened"
        initView(mCameraDevice!!.property)
        mTextView_test!!.text = cameraProperty
    }

    override fun onCameraClosed() {
        Log.d(TAG, "onCameraClosed")
        mTextView_captureState!!.text = "onCameraClosed"
        mTextView_test!!.text = cameraProperty
    }

    override fun onCaptureStarted() {
        Log.d(TAG, "onCaptureStarted")
        mTextView_captureState!!.text = "onCaptureStarted"
        mTextView_test!!.text = cameraProperty
    }

    override fun onCaptureStopped() {
        Log.d(TAG, "onCaptureStopped")
        mTextView_captureState!!.text = "onCaptureStopped"
        mTextView_test!!.text = cameraProperty
    }

    override fun onPreviewStarted() {
        Log.d(TAG, "onPreviewStarted")
        mTextView_captureState!!.text = "onPreviewStarted"
        mTextView_test!!.text = cameraProperty
    }

    override fun onPreviewStopped() {
        Log.d(TAG, "onPreviewStopped")
        mTextView_captureState!!.text = "onPreviewStopped"
        mTextView_test!!.text = cameraProperty
    }

    override fun onRecordStarted() {
        Log.d(TAG, "onRecordStarted")
        mTextView_captureState!!.text = "onRecordStarted"
        mTextView_test!!.text = cameraProperty
    }

    override fun onRecordStopped() {
        Log.d(TAG, "onRecordStopped")
        mTextView_captureState!!.text = "onRecordStopped"
        mTextView_test!!.text = cameraProperty
    }

    override fun onPictureCompleted() {
        Log.d(TAG, "onPictureCompleted")
        mTextView_captureState!!.text = "onPictureCompleted"
        mTextView_test!!.text = cameraProperty
    }

    override fun onPermissionGrantResult(permission: String, grantResult: Int) {
        Snackbar.make(
            activity!!.window.decorView,
            permission + " is " + if (PermissionHelper.PERMISSION_GRANTED == grantResult) "GRANTED" else "DENIED",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onCaptureData(timestamp: Long, data: ByteBuffer) {
        mTextView_captureState!!.text = "onCaptureData(ByteBuffer):" + data.limit()
        mCalcurationRate_framerate!!.updata()
    }

    private inner class CaptureInfoAdapter(
        _context: Context?,
        _textViewResourceId: Int,
        _captureInfoList: List<IntArray>?
    ) :
        ArrayAdapter<Any?>(_context!!, _textViewResourceId, _captureInfoList!!) {
        private var context: Context? = null
        private var textViewResourceId = 0
        private var captureInfoList: List<IntArray>? = null

        init {
            context = _context
            textViewResourceId = _textViewResourceId
            captureInfoList = _captureInfoList
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = super.getView(position, convertView, parent) as TextView
            if (null != captureInfoList) {
                val captureInfo = captureInfoList.get(position)
                v.text =
                    captureInfo[0].toString() + "x" + captureInfo[1] + ", " + captureInfo[2] + "[fps]"
            } else {
                v.text = "Unknown"
            }
            return v
        }

        override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
            val v = super.getDropDownView(position, convertView, parent) as TextView
            if (null != captureInfoList) {
                val captureInfo = captureInfoList.get(position)
                v.text =
                    captureInfo[0].toString() + "x" + captureInfo[1] + ", " + captureInfo[2] + "[fps]"
            } else {
                v.text = "Unknown"
            }
            return v
        }
    }

    private fun initView(property: CameraProperty?) {
        if (null == property) {
            Log.w(TAG, "CameraProperty is null...")
            return
        }
        mSpinner_captureInfo!!.adapter = CaptureInfoAdapter(
            mContext,
            R.layout.simple_spinner_item,
            mCameraDevice!!.property.supportedCaptureInfo
        )
        mSeekBar_exposure!!.max = property.exposureStepMax - property.exposureStepMin
        mSeekBar_brightness!!.max = property.brightnessMax - property.brightnessMin
        mSeekBar_focus!!.max = property.focusDistanceMax - property.focusDistanceMin
        mSeekBar_cameraGain!!.max = property.gainMax - property.gainMin
        updateView()
    }

    internal inner class CalcurationRate(_textView: TextView?) {
        var textView: TextView? = null
        var count = 0
        var startTime: Long = 0
        var endTime: Long = 0
        var rate = 0f

        init {
            textView = _textView
        }

        fun start() {
            count = 0
            startTime = System.currentTimeMillis()
            endTime = 0
            rate = 0f
        }

        fun updata() {
            endTime = System.currentTimeMillis()
            count++
            if (endTime - startTime > 1000) {
                rate = (count * 1000 / (endTime - startTime)).toFloat()
                startTime = endTime
                count = 0
                textView!!.text = rate.toString()
            }
        }

        fun finish() {
            count = 0
            startTime = 0
            endTime = 0
            rate = 0f
        }
    }

    private val cameraProperty: String?
        private get() {
            var str: String? = ""
            val property = mCameraDevice!!.property
            if (null != property) {
                str += "info :" + property.captureSize[0] + ", " + property.captureSize[1] + ", " + property.captureFps + ", " + property.captureDataFormat + System.lineSeparator()
                str += "expo :" + property.exposureMode + ", " + property.exposureStep + ", bright:" + property.brightness + System.lineSeparator()
                str += "WB   :" + property.whiteBalanceMode + ", PLF  :" + property.powerLineFrequencyControlMode + ", Indi :" + property.indicatorMode + System.lineSeparator()
                str += "Focus:" + property.focusMode + ", " + property.focusDistance + ", Gain  :" + property.gain + System.lineSeparator()
            } else str = null
            return str
        }

    private fun updateView() {
        val property = mCameraDevice!!.property ?: return

        // exposure
        if (property.exposureMode == CameraProperty.EXPOSURE_MODE_AUTO) {
            mSwitch_autoExposure!!.isChecked = true
            mTextView_exposure!!.text = "-"
        } else if (property.exposureMode == CameraProperty.EXPOSURE_MODE_MANUAL) {
            mSwitch_autoExposure!!.isChecked = false
            mSeekBar_exposure!!.progress = property.exposureStepMin + property.exposureStep
            mTextView_exposure!!.text =
                "Exposure:" + property.exposureStep + ":" + "(" + property.exposureStepMin + " - " + property.exposureStepMax + ")"
        }

        // brightness
        mSeekBar_brightness!!.progress = property.brightnessMin + property.brightness
        mTextView_brightness!!.text =
            "Brightness:" + property.brightness + ":" + "(" + property.brightnessMin + " - " + property.brightness + ")"

        // whitebalance
        if (property.exposureMode == CameraProperty.EXPOSURE_MODE_AUTO) {
            mSwitch_autoExposure!!.isChecked = true
            mTextView_exposure!!.text = "-"
        } else if (property.exposureMode == CameraProperty.EXPOSURE_MODE_MANUAL) {
            mSwitch_autoExposure!!.isChecked = false
            mSeekBar_exposure!!.progress = property.exposureStepMin + property.exposureStep
            mTextView_exposure!!.text =
                "Exposure:" + property.exposureStep + ":" + "(" + property.exposureStepMin + " - " + property.exposureStepMax + ")"
        }
        when (property.whiteBalanceMode) {
            CameraProperty.WHITE_BALANCE_MODE_AUTO -> mRadioGroup_whitebalance!!.check(R.id.radioButton_auto)
            CameraProperty.WHITE_BALANCE_MODE_CLOUDY_DAYLIGHT -> mRadioGroup_whitebalance!!.check(R.id.radioButton_cloudyDaylight)
            CameraProperty.WHITE_BALANCE_MODE_DAYLIGHT -> mRadioGroup_whitebalance!!.check(R.id.radioButton_daylight)
            CameraProperty.WHITE_BALANCE_MODE_FLUORESCENT -> mRadioGroup_whitebalance!!.check(R.id.radioButton_fluorescent)
            CameraProperty.WHITE_BALANCE_MODE_INCANDESCENT -> mRadioGroup_whitebalance!!.check(R.id.radioButton_incandescent)
            CameraProperty.WHITE_BALANCE_MODE_TWILIGHT -> mRadioGroup_whitebalance!!.check(R.id.radioButton_twilight)
            else -> {}
        }
        mTextView_whitebalance!!.text =
            "White balance:" + property.whiteBalanceMode + "," + property.whiteBalanceTemperature
        when (property.powerLineFrequencyControlMode) {
            CameraProperty.POWER_LINE_FREQUENCY_CONTROL_MODE_50HZ -> mRadioGroup_powerLineFrequency!!.check(
                R.id.radioButton_50Hz
            )

            CameraProperty.POWER_LINE_FREQUENCY_CONTROL_MODE_60HZ -> mRadioGroup_powerLineFrequency!!.check(
                R.id.radioButton_60Hz
            )

            CameraProperty.POWER_LINE_FREQUENCY_CONTROL_MODE_DISABLE -> mRadioGroup_powerLineFrequency!!.check(
                R.id.radioButton_disable
            )

            else -> {}
        }
        mTextView_powerLineFrequency!!.text =
            "Power line frequency:" + property.powerLineFrequencyControlMode
        when (property.captureDataFormat) {
            CameraProperty.CAPTURE_DATA_FORMAT_RGB_565 -> mRadioGroup_captureFormat!!.check(R.id.radioButton_rgb565)
            CameraProperty.CAPTURE_DATA_FORMAT_ARGB_8888 -> mRadioGroup_captureFormat!!.check(R.id.radioButton_argb8888)
            CameraProperty.CAPTURE_DATA_FORMAT_YUY2 -> mRadioGroup_captureFormat!!.check(R.id.radioButton_yuy2)
            CameraProperty.CAPTURE_DATA_FORMAT_H264 -> mRadioGroup_captureFormat!!.check(R.id.radioButton_h264)
            else -> {}
        }
        mTextView_captureFormat!!.text = "Capture format:" + property.captureDataFormat
        when (property.indicatorMode) {
            CameraProperty.INDICATOR_MODE_AUTO -> mRadioGroup_indicatorMode!!.check(R.id.radioButton_indicatorMode_auto)
            CameraProperty.INDICATOR_MODE_ON -> mRadioGroup_indicatorMode!!.check(R.id.radioButton_indicatorMode_on)
            CameraProperty.INDICATOR_MODE_OFF -> mRadioGroup_indicatorMode!!.check(R.id.radioButton_indicatorMode_off)
            else -> {}
        }
        mTextView_indicatorMode!!.text = "Capture format:" + property.indicatorMode

        // focus
        if (property.focusMode == CameraProperty.FOCUS_MODE_AUTO) {
            mSwitch_autoFocus!!.isChecked = true
            mTextView_focus!!.text = "-"
        } else if (property.focusMode == CameraProperty.FOCUS_MODE_MANUAL) {
            mSwitch_autoFocus!!.isChecked = false
            mSeekBar_focus!!.progress = property.focusDistanceMin + property.focusDistance
            mTextView_focus!!.text =
                "Focus:" + property.focusDistance + ":" + "(" + property.focusDistanceMin + " - " + property.focusDistanceMax + ")"
        }

        // gain
        mSeekBar_cameraGain!!.progress = property.gainMin + property.gain
        mTextView_cameraGain!!.text =
            "Gain:" + property.gain + ":" + "(" + property.gainMin + " - " + property.gainMax + ")"
    }
}
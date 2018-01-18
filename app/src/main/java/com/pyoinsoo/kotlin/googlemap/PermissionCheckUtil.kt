package com.pyoinsoo.kotlin.googlemap

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity

/*
 * Created by pyoinsoo on 2018-01-18.
 * insoo.pyo@gmail.com
 * 위치정보사용을 위한 Runtime Permission Check
 * 유틸클래스(Top-Level클래스 및 함수로 구성한다)
 */

//요청코드값
const val REQUEST_CODE = 500


/*
 * 다음 메소드를 실행하면 대화 상자가 나오며
 * Runtime Permission Check
 * 를 진행한다
 */
internal fun requestPermission(activity: AppCompatActivity) {
    ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE)
}

/*
 * 사용자가 위치 권한을 부여했는지 확인하는 함수
 */
internal fun isPermissionGranted(grantPermissions: Array<String>,
                        grantResults: IntArray): Boolean {
    val permissionSize = grantPermissions.size
    for (i in 0 until permissionSize) when {
            Manifest.permission.ACCESS_FINE_LOCATION == grantPermissions[i] ->
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
        }
    return false
}

/*
 * 단말기 위치설정화면으로 유도하기 위한 대화상자화면
 */
class LocationSettingDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(activity)
                .setMessage("단말기 위치설정이 필요합니다.")
                .setPositiveButton("확인") { _ , _ ->
                    /*
                     * 사용자 단말기에 위치설정이 되어있지않으면
                     * 위치설정으로 이동한 후 앱을 진행한다
                     */
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }.create()
    }
    companion object {
        /*
         * 현 대화상자를 생성
         */
        fun newInstance(): LocationSettingDialog {
            return LocationSettingDialog()
        }
    }
}
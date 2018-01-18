package com.pyoinsoo.kotlin.googlemap

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log

/*
 * Created by pyoinsoo on 2018-01-18.
 * insoo.pyo@gmail.com
 */
class SplashActivity : AppCompatActivity(){

    private val handler = Handler()
    private var isNetworkLocation: Boolean = false
    private var isGPSLocation:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mListener = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if( mListener != null){
            /*
             * 현재 단말기의 위치설정 여부를 확인한다
             */
            isGPSLocation = mListener.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkLocation = mListener.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            Log.d("gps,network","$isGPSLocation , $isNetworkLocation")
        }

        handler.postDelayed({
          /*
           * GPS 또는 Network로 위치설정이 되었다면 바로 이동한다
           */
          if(isGPSLocation){
              val intent = Intent(this, MainActivity::class.java )
              intent.putExtra("provider", LocationManager.GPS_PROVIDER)
              startActivity(intent)
              finish()
          }else if(isNetworkLocation){
              val intent = Intent(this, MainActivity::class.java)
              intent.putExtra("provider", LocationManager.NETWORK_PROVIDER)
              startActivity(intent)
              finish()
          }else{
              /*
               * 단말기에 위치설정이 되어있지않으면 위치설정 화면으로 이동한다
               * PermissionCheckUtil kotlin 파일의 Top-Level 클래스를 호출
               */
              LocationSettingDialog.newInstance().show(supportFragmentManager,"위치설정")
          }
        },1500)
    }

    override fun onRestart() {
        super.onRestart()
        /*
         * 위치설정이 되어있지않아 사용자가 위치설정을
         * 강제한 후 Back Key를 눌렀을때 실행되는 코드
         */
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
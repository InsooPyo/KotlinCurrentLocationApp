## Using FusedLocationProviderClient
- Find your current location using FusedLocationProviderClient
- 앱 시작시 현재위치를 찾는 앱
- [Add in Google LBS API  version 11.8.0](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient)
## Be sure to read(AndroidMenifest.xml)
- [Google Developer Console](https://console.developers.google.com/)에서 Google Map Key를 발급 받으세요
- [Google Developer Console](https://console.developers.google.com/)의 사용자 인증정보 링크에서 본 앱의 Package 명을 등록해 주세요
- AndroidMenifest.xml 에 발급받은 Google Map Key를 등록해 주세요
```xml
<meta-data android:name="com.google.android.geo.API_KEY"       
            android:value="GoogleMap Key"/>
```

## build.gradle(App module)
```gradle
compile 'com.google.android.gms:play-services-location:11.8.0'
compile 'com.google.android.gms:play-services-maps:11.8.0'
```

## 핵심 코드(MainActivity.kt)
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
         * SplashActivity.kt에서 현재 단말기에 설정된 위치제공자를 가져온다
         */
        currentProvider = intent.getStringExtra("provider")

        /*
         * 5.0이상일 경우 위치퍼미션에 대한 사용자 허락을 받는다
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkMyPermissionLocation()
        } else {
            initGoogleMapLocation()
        }
        val mapFragment = 
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        /*
         * 비동기 방식으로 GoogleMap 초기설정을 진행한다
         */
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap
            mMap.uiSettings.isZoomControlsEnabled = true

            val options = MarkerOptions()
            /*
             * 처음 위치를 적도로 놓는다
             */
            options.position(LatLng(0.0, 0.0))
            /*
             * 마커등록
             */
            options.icon(
               BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            val marker = mMap.addMarker(options)

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position,1f))
        }
    }
    /*
     * Permmission Check여부를 확인하는 메소드
     */
    private fun checkMyPermissionLocation() {
        /*
         * Permission 허락을 받지않았다면 Permission Check를 진행
         */
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            /*
             * PermissionCheckUtil.kt 파일의 함수를 호출
             */
            requestPermission(this)

        } else { //Permission 허락을 받은 상태라면 위치 정보 초기화를 진행한다
            initGoogleMapLocation()
        }
    }
    /*
     * 위치 이벤트에 대한 콜백을 제공.
     * 단말기위치정보가 update되면 자동으로 호출
     * FusedLocationProviderApi에 등록된
     * 위치알림을 수신하는 데 사용
     */
    private  val mLocationCallback = object : LocationCallback(){
        /*
         *  성공적으로 위치정보와 넘어왔을때를 동작하는 Call back 함수
         */
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            mCurrentLocation = result!!.locations[0]

            val options = MarkerOptions()
            options.position(LatLng(mCurrentLocation.latitude, mCurrentLocation.longitude))
            val icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
            options.icon(icon)
            val marker = mMap.addMarker(options)

            /*
             * 단말기 현재 위치로 이동한다
             */
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    marker.position,
                    16f
            ))
            /*
             * 지속적으로 위치정보를 받으려면
             * mLocationRequest.numUpdates = 1을 주석처리하고
             * 밑에 코드 주석을 푼다
             */
            //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        /*
         * 현재 콜백이 동작가능한지에 대한 여부
         */
        override fun onLocationAvailability(availability: LocationAvailability?) {
            //boolean isLocation = availability.isLocationAvailable();
        }
    }
    /*
     * 현재 위치를 알아내는 코드구성
     */
    @SuppressLint("MissingPermission")
    fun initGoogleMapLocation(){
        /*
         * FusedLocationProviderApi에서
         * 위치 업데이트를위한 서비스 품질등 다양한요청을
         * 설정하는데 사용하는 데이터객체인
         * LocationRequest를 획득
         */
        mLocationRequest = LocationRequest()
        /*
         *위치가 update되는 주기
         */
        mLocationRequest.interval = 10000
        /*
         * 위치 획득후 update되는 주기
         */
        mLocationRequest.fastestInterval = 10000

        /*
         * update되는 횟수 여기선 1번만 설정한다
         */
        mLocationRequest.numUpdates = 1

        if (currentProvider.equals(LocationManager.GPS_PROVIDER, ignoreCase = true)) {
            //배터리소모에 상관없이 정확도를 최우선으로 고려
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        } else {
            //배터리와 정확도의 밸런스를 고려하여 위치정보를 획득(정확도 다소 높음)
            mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        /*
         * 위치서비스 설정 정보를 저장하기 위한 빌더객체획득
         */
        val builder = LocationSettingsRequest.Builder()
        /*
         * 현재 위치정보 Setting정보가 저장된 LocationRequest
         * 객체를 등록
         */
        builder.addLocationRequest(mLocationRequest)

        /*
         * 위치정보 요청을 수행하기 위해 단말기에서
         * 관련 시스템 설정(Gps,Network)이 활성화되었는지 확인하는 클래스인
         * SettingClient를 획득한다
         */

        val mSettingsClient = LocationServices.getSettingsClient(this)

        /*
         * 위치 서비스 유형을 저장하고
         * 위치 설정에도 사용하기위해
         * LocationSettingsRequest 객체를 획득
         */
        val mLocationSettingsRequest = builder.build()
        val locationResponse = mSettingsClient.checkLocationSettings(mLocationSettingsRequest)

        /*
         * 현재 위치제공자(Provider)와 상호작용하는 진입점인
         * FusedLocationProviderClient 객체를 획득
         */
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /*
         * 정상적으로 위치정보가 설정되었다면
         * 위치업데이트를 요구하고, 설정이 잘못되었다면
         * Log를 출력한다
         */
        with(locationResponse){
           //위치정보획득 성공시
            addOnSuccessListener{
                Log.d("Response", "Success!!")
                mFusedLocationClient?.requestLocationUpdates(
                        mLocationRequest, mLocationCallback, Looper.myLooper())
            }
            //위치정보 획득 실패시
            addOnFailureListener{
                when ((it as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> 
                         Log.e("onFailure", "위치환경체크")
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> 
                        Log.e("onFailure", "위치설정체크요망")
                }
            }
        }
    }
    /*
     * 사용자가 PermmissionC Check 대화상자(허락,거부)에서 선택한 결과를
     * 처리하는 콜백 메소드
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        //요청코드가 맞지 않는다면
        if (requestCode != REQUEST_CODE) {
            return
        }
        /*
         * PermissionCheckUtil.kt 파일의 isPermissionGranted 함수를 호출
         */
        if (isPermissionGranted(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),                                               grantResults)) {
            //허락을 받았다면 위치값을 알아오는 코드를 진행
            initGoogleMapLocation()
        } else { //사용자가 허락하지 않을경우
            Toast.makeText(this, "위치정보사용을 허락 하지않아 앱을 중지합니다",
                    Toast.LENGTH_SHORT).show()
            //finish();
        }
    }
    /*
     * 현재 화면을 나갈때 반드시 등록된
     * 위치정보 알림을 제거
     */
    override fun onStop() {
        super.onStop()
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }
```
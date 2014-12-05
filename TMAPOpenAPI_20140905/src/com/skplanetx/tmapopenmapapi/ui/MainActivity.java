package com.skplanetx.tmapopenmapapi.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skp.Tmap.BizCategory;
import com.skp.Tmap.TMapCircle;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapData.BizCategoryListenerCallback;
import com.skp.Tmap.TMapData.ConvertGPSToAddressListenerCallback;
import com.skp.Tmap.TMapData.FindAllPOIListenerCallback;
import com.skp.Tmap.TMapData.FindAroundNamePOIListenerCallback;
import com.skp.Tmap.TMapData.FindPathDataAllListenerCallback;
import com.skp.Tmap.TMapData.FindPathDataListenerCallback;
import com.skp.Tmap.TMapData.TMapPathType;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapGpsManager.onLocationChangedCallback;
import com.skp.Tmap.TMapInfo;
import com.skp.Tmap.TMapLabelInfo;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapMarkerItem2;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapPolygon;
import com.skp.Tmap.TMapTapi;
import com.skp.Tmap.TMapView;
import com.skp.Tmap.TMapView.MapCaptureImageListenerCallback;
import com.skp.Tmap.TMapView.TMapLogoPositon;
import com.skplanetx.tmapopenmapapi.LogManager;
import com.skplanetx.tmapopenmapapi.R;

public class MainActivity extends BaseActivity implements onLocationChangedCallback
{
	@Override
	public void onLocationChange(Location location) {
		LogManager.printLog("onLocationChange " + location.getLatitude() +  " " + location.getLongitude());
		if(m_bTrackingMode) {
			mMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
		}
	}

	private TMapView		mMapView = null;
	

	private TMapPoint startpoint = null;
	private TMapPoint endpoint= null;
	private TMapPolyLine polyLine=null; //그림그리는 객체
	private TMapPoint pointA ;
	private TMapData tmapdata ; 
	private ArrayList<String> RoutePoint = new ArrayList<String>();
	
	private Context 		mContext;
	private ArrayList<Bitmap> mOverlayList;
	private ImageOverlay mOverlay;

	public static String mApiKey; // 발급받은 appKey
	public static String mBizAppID; // 발급받은 BizAppID (TMapTapi로 TMap앱 연동을 할 때 BizAppID 꼭 필요)
	
	private static final int[] mArrayMapButton = {

		R.id.btnAnimateTo,
		R.id.btnSetIcon,
		R.id.btnSetCompassMode,

	
	};

	private 	int 		m_nCurrentZoomLevel = 0;
	private 	double 		m_Latitude  = 0;
	private     double  	m_Longitude = 0;
	private 	boolean 	m_bShowMapIcon = false;

	private 	boolean 	m_bTrafficeMode = false;
	private 	boolean 	m_bSightVisible = false;
	private 	boolean 	m_bTrackingMode = false;
	
	private 	boolean 	m_bOverlayMode = false;
	
	ArrayList<String>		mArrayID;
	
	ArrayList<String>		mArrayCircleID;
	private static 	int 	mCircleID;
	
	ArrayList<String>		mArrayLineID;
	private static 	int 	mLineID;
	
	ArrayList<String>		mArrayPolygonID;
	private static  int 	mPolygonID;

	ArrayList<String>       mArrayMarkerID;
	private static int 		mMarkerID;
	
	TMapGpsManager gps = null;

	
	/**
	 * onCreate() 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_activity);

		startActivity(new Intent(this,LoadingActivity.class));
		
		mContext = this;
		
		mMapView = new TMapView(this);
		addView(mMapView);
		
		configureMapView();
		
		initView();
		
		mArrayID = new ArrayList<String>();
		
		mArrayCircleID = new ArrayList<String>();
		mCircleID = 0;
		
		mArrayLineID = new ArrayList<String>();
		mLineID = 0;
		
		mArrayPolygonID = new ArrayList<String>();
		mPolygonID = 0;
		
		mArrayMarkerID	= new ArrayList<String>();
		mMarkerID = 0;
		
		
		gps = new TMapGpsManager(MainActivity.this);
		gps.setMinTime(1000);
		gps.setMinDistance(5);
		
		gps.setProvider(gps.NETWORK_PROVIDER);
		gps.OpenGps();
		
		mMapView.setTMapLogoPosition(TMapLogoPositon.POSITION_BOTTOMRIGHT);
LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		Location location = locationManager
	                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		double latitude = location.getLatitude();
	    double longitude = location.getLongitude();  
	    
		TMapPoint point = new TMapPoint(latitude, longitude);
		mMapView.setCenterPoint(point.getLongitude(),point.getLatitude());
		mMapView.setIconVisibility(true);
		mMapView.setLocationPoint(point.getLongitude(),point.getLatitude());
	}

	/**
	 * setSKPMapApiKey()에 ApiKey를 입력 한다.
	 * setSKPMapBizappId()에 mBizAppID를 입력한다.
	 * -> setSKPMapBizappId는 TMapTapi(TMap앱 연동)를 사용할때 BizAppID 설정 해야 한다. TMapTapi 사용하지 않는다면 setSKPMapBizappId를 하지 않아도 된다.
	 */
	private void configureMapView() {
		mMapView.setSKPMapApiKey("1ca0c796-467a-34a5-bceb-773d01c98ae0");
		mMapView.setSKPMapBizappId(mBizAppID);
	}

	/**
	 * initView - 버튼에 대한 리스너를 등록한다. 
	 */
	private void initView() {	
		for (int btnMapView : mArrayMapButton) {
			Button ViewButton = (Button)findViewById(btnMapView);
			ViewButton.setOnClickListener(this);
		}

		mMapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
			@Override
			public void SKPMapApikeySucceed() {
				LogManager.printLog("MainActivity SKPMapApikeySucceed");
			}
			
			@Override
			public void SKPMapApikeyFailed(String errorMsg) {
				LogManager.printLog("MainActivity SKPMapApikeyFailed " + errorMsg);
			}
		});
		
		mMapView.setOnBizAppIdListener(new TMapView.OnBizAppIdListenerCallback() {
			@Override
			public void SKPMapBizAppIdSucceed() {
				LogManager.printLog("MainActivity SKPMapBizAppIdSucceed");
			}
			
			@Override
			public void SKPMapBizAppIdFailed(String errorMsg) {
				LogManager.printLog("MainActivity SKPMapBizAppIdFailed " + errorMsg);
			}
		});

		
		mMapView.setOnEnableScrollWithZoomLevelListener(new TMapView.OnEnableScrollWithZoomLevelCallback() {
			@Override
			public void onEnableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
				LogManager.printLog("MainActivity onEnableScrollWithZoomLevelEvent " + zoom + " " + centerPoint.getLatitude() + " " + centerPoint.getLongitude());
			}
		});

		mMapView.setOnDisableScrollWithZoomLevelListener(new TMapView.OnDisableScrollWithZoomLevelCallback() {
			@Override
			public void onDisableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
				LogManager.printLog("MainActivity onDisableScrollWithZoomLevelEvent " + zoom + " " + centerPoint.getLatitude() + " " + centerPoint.getLongitude());
			}
		});
		
		mMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
			@Override
			public boolean onPressUpEvent(ArrayList<TMapMarkerItem> markerlist,ArrayList<TMapPOIItem> poilist, TMapPoint point, PointF pointf) {
				LogManager.printLog("MainActivity onPressUpEvent " + markerlist.size());
				return false;
			}
			
			@Override
			public boolean onPressEvent(ArrayList<TMapMarkerItem> markerlist,ArrayList<TMapPOIItem> poilist, TMapPoint point, PointF pointf) {
				LogManager.printLog("MainActivity onPressEvent " + markerlist.size());

				for (int i = 0; i < markerlist.size(); i++) {
					TMapMarkerItem item = markerlist.get(i);
					LogManager.printLog("MainActivity onPressEvent " + item.getName() + " " + item.getTMapPoint().getLatitude() + " " + item.getTMapPoint().getLongitude());
				}
				return false;
			}
		});
		
		mMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
			@Override
			public void onLongPressEvent(ArrayList<TMapMarkerItem> markerlist,ArrayList<TMapPOIItem> poilist, TMapPoint point) {
				LogManager.printLog("MainActivity onLongPressEvent " + markerlist.size());
				
				pointA = point;
				
	            //TMapPoint endpoint = new TMapPoint(point.getLatitude(), point.getLongitude());
	            
				//hana
				// 출발지, 도착지 선택 알림창 부분.
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				
				builder.setTitle("출발지/도착지를 선택하세요"); // 제목 설정
				
				builder.setCancelable(true); // 뒤로 버튼 클릭시 취소 가능 설정
				builder.setPositiveButton("출발", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// 출발 버튼 클릭시 설정
						startpoint = new TMapPoint(pointA.getLatitude(), pointA.getLongitude());		
						
					}
				});
				
				builder.setNegativeButton("도착", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// 도착 버튼 클릭시 설정
						endpoint = new TMapPoint(pointA.getLatitude(), pointA.getLongitude());
						tmapdata = new TMapData(); 
						
						if(startpoint == null)
						{
							///////////////////////////여기가 안돼요
							Toast.makeText(getApplicationContext(), "출발 확인", Toast.LENGTH_LONG).show();
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
							builder.setTitle("출발지를 선택해 주세요") ;
							
							builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									dialog.cancel();
								}
							});
						}else{
						}
						
							// 경로설정
							try {
								polyLine = tmapdata.findPathDataWithType(TMapPathType.BICYCLE_PATH, startpoint, endpoint);
								
								for (TMapPoint temp : polyLine.getLinePoint())
								{
									LogManager.printLog("point "+temp.toString());
									RoutePoint.add(temp.toString());
								}
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ParserConfigurationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (FactoryConfigurationError e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SAXException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			            
							mMapView.addTMapPolyLine("Go Home",polyLine);
							
						
					}
				});
				

				AlertDialog dialog = builder.create();    // 알림창 객체 생성
				dialog.show();    // 알림창 띄우기
			}
		});
		
		mMapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
			@Override
			public void onCalloutRightButton(TMapMarkerItem markerItem) {
				String strMessage = "";
				strMessage = "ID: " + markerItem.getID() + " " + "Title " + markerItem.getCalloutTitle();
				Common.showAlertDialog(MainActivity.this, "Callout Right Button", strMessage);
			}
		});
		
		mMapView.setOnClickReverseLabelListener(new TMapView.OnClickReverseLabelListenerCallback() {
			@Override
			public void onClickReverseLabelEvent(TMapLabelInfo findReverseLabel) {
				if(findReverseLabel != null) {
					LogManager.printLog("MainActivity setOnClickReverseLabelListener " + findReverseLabel.id + " / " + findReverseLabel.labelLat
							 + " / " + findReverseLabel.labelLon + " / " + findReverseLabel.labelName);

				}
			}
		});
		
		m_nCurrentZoomLevel = -1;
		m_bShowMapIcon = false;
		m_bTrafficeMode = false;
		m_bSightVisible = false;
		m_bTrackingMode = false;	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		gps.CloseGps();
		if(mOverlayList != null){
			mOverlayList.clear();
		}
		//Debug.stopMethodTracing();
		System.gc() ;
	}
	
	/**
	 * onClick Event 
	 */
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		
		case R.id.btnAnimateTo		  : 	animateTo(); 			break;
		case R.id.btnSetIcon		  :		goSelectActivity();			break; //나중에 수정하기
		case R.id.btnSetCompassMode   :		setCompassMode();		break;
		}
	} 
	
	public TMapPoint randomTMapPoint() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		Location location = locationManager
	                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		double latitude = location.getLatitude();//((double)Math.random() ) * (37.575113-37.483086) + 37.483086;
	    double longitude = location.getLongitude();//((double)Math.random() ) * (127.027359-126.878357) + 126.878357;    

	    
		TMapPoint point = new TMapPoint(latitude, longitude);
		
		return point;
	}
	
	public void overlay() {
		m_bOverlayMode = !m_bOverlayMode;
		if(m_bOverlayMode) {
			mMapView.setZoomLevel(6);
			
			if(mOverlay == null){
				mOverlay = new ImageOverlay(this, mMapView);
			}
			
			mOverlay.setLeftTopPoint(new TMapPoint(45.640171, 114.9652948));
			mOverlay.setRightBottomPoint(new TMapPoint(29.2267177, 138.7206798));
			mOverlay.setImage(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.test_image));
			
			if(mOverlayList == null){
				mOverlayList = new ArrayList<Bitmap>();
				mOverlayList.add(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.test_image));
				mOverlayList.add(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ani1));
				mOverlayList.add(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ani2));
			}
			
			mOverlay.setAnimationIcons(mOverlayList);
			mOverlay.setAniDuration(10000);
			mOverlay.startAnimation();
			mMapView.addTMapOverlayID(0, mOverlay);
		} else {
			mOverlay.stopAnimation();
			mMapView.removeTMapOverlayID(0);
		}
	}
	public void goSelectActivity()
	{
		Intent intent = new Intent(getApplicationContext(), SelectActivity.class);
		
		//intent.putStringArrayListExtra("route", tmapdata);
		intent.putStringArrayListExtra("RoutePoint", RoutePoint);
		startActivity(intent);
	}
	public void animateTo() {
		TMapPoint point = randomTMapPoint();
		mMapView.setIconVisibility(true);
		
		mMapView.setCenterPoint(point.getLongitude(), point.getLatitude(), true);
		mMapView.setLocationPoint(point.getLongitude(), point.getLatitude());
	}
	
	public Map<String, AroundusItems> mAroundusItemHash = new LinkedHashMap<String, AroundusItems>();
	
	public void setAroundus() {
		AroundusJsonItem aroundusItem = requestAroundersList();
		addAroundersMarker(aroundusItem);
	}
	
	public AroundusJsonItem requestAroundersList() {
		TMapPoint center = mMapView.getCenterPoint();

		TMapPoint rightBottom = mMapView.getRightBottomPoint();
		TMapPoint leftTop = mMapView.getLeftTopPoint();
		
		AroundusJsonItem aroundusItem = null;

		StringBuilder url = new StringBuilder();
		url.append("adRequest?version=1");
		url.append("&centerLat=").append(center.getLatitude());
		url.append("&centerLon=").append(center.getLongitude());
		url.append("&reqCoordType=").append("WGS84GEO");
		url.append("&resCoordType=").append("WGS84GEO");
		url.append("&rightLat=").append(rightBottom.getLatitude());
		url.append("&rightLon=").append(rightBottom.getLongitude());
		url.append("&leftLat=").append(leftTop.getLatitude());
		url.append("&leftLon=").append(leftTop.getLongitude());
		url.append("&countReqAd=").append(10);
		url.append("&formAd=0");
		
		try {
			aroundusItem = new AroundusJsonItem(url);
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return aroundusItem;
	}
	
	public void addAroundersMarker(AroundusJsonItem aroundusItem) {
		final int width = getWindowManager().getDefaultDisplay().getWidth();
		final int height = getWindowManager().getDefaultDisplay().getHeight();

		try {
			for(int i = 0; i < aroundusItem.getPoiItems().size(); i++) {
				AroundusItems poiItems = aroundusItem.getPoiItems().get(i);
	
				Bitmap bgBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.poi_img);
				Bitmap iconBitmap = BitmapFactory.decodeStream(new URL(poiItems.getIcon()).openStream());
				Bitmap marker = overlayMark(bgBitmap, iconBitmap, width, height);
				
				TMapPoint point = new TMapPoint(poiItems.getLatitude(), poiItems.getLongitude());
				AroundusOverlay item = new AroundusOverlay(mContext, mMapView);
	
				String strID = String.format("%02d", i);
				item.setID(strID);
				item.setTMapPoint(point);	
				item.setIcon(marker);
				item.setPosition(0.5f, 1.0f);
	
				iconBitmap = Bitmap.createScaledBitmap(iconBitmap, 100, 100, true);
				item.setLeftImage(iconBitmap);
				item.setTitle(poiItems.getCompany());
				item.setSubTitle(poiItems.getPromotion());
	
				mAroundusItemHash.put(strID, poiItems);
				mMapView.addMarkerItem2(strID, item);
	
				mMapView.setOnMarkerClickEvent(new TMapView.OnCalloutMarker2ClickCallback() {
					@Override
					public void onCalloutMarker2ClickEvent(String id, TMapMarkerItem2 markerItem2) {
						LogManager.printLog("ClickEvent " + " id " + id + " " + mAroundusItemHash.get(id).getUrl());
						LogManager.printLog("ClickEvent " + " id " + id + " " + mAroundusItemHash.get(id).getAdClickKey());
	
						Intent intent = new Intent(getBaseContext(), WebActivity.class);
						intent.putExtra("URL", mAroundusItemHash.get(id).getUrl());
						intent.putExtra("ADCLICKKEY", mAroundusItemHash.get(id).getAdClickKey());
						startActivity(intent);
					}
				});
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public Bitmap overlayMark(Bitmap bmp1, Bitmap bmp2, int width, int height) {
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
		
		int marginLeft = 7;
		int marginTop = 5;

		if(width >= 1500 || height > 1500) {
			bmp2 = Bitmap.createScaledBitmap(bmp2, bmp1.getWidth() - 40, bmp1.getHeight() - 50, true);
			marginLeft = 20;
			marginTop = 10;
		} else if(width >= 1200 || height > 1200) {
			bmp2 = Bitmap.createScaledBitmap(bmp2, bmp1.getWidth() - 22, bmp1.getHeight() - 35, true);
			marginLeft = 11;
			marginTop = 7;
		} else {
			bmp2 = Bitmap.createScaledBitmap(bmp2, bmp1.getWidth() - 15, bmp1.getHeight() - 25, true);
		}
		
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp1, 0, 0, null);
		canvas.drawBitmap(bmp2, marginLeft, marginTop, null);
		return bmOverlay;
	}
	
	/**
	 * mapZoomIn
	 * 지도를 한단계 확대한다. 
	 */
	public void mapZoomIn() {
		mMapView.MapZoomIn();   
	}
	
	/**
	 * mapZoomOut
	 * 지도를 한단계 축소한다. 
	 */
	public void mapZoomOut() {
		mMapView.MapZoomOut();
	}
	
	/**
	 * getZoomLevel
	 * 현재 줌의 레벨을 가지고 온다. 
	 */
	public void getZoomLevel() {
		int nCurrentZoomLevel = mMapView.getZoomLevel();
		Common.showAlertDialog(this, "", "현재 Zoom Level : " + Integer.toString(nCurrentZoomLevel));
	}
	
	/**
	 * setZoomLevel
	 * Zoom Level을 설정한다. 
	 */
	public void setZoomLevel() {
    	final String[] arrString = getResources().getStringArray(R.array.a_zoomlevel);
		AlertDialog dlg = new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle("Select Zoom Level")
			.setSingleChoiceItems(R.array.a_zoomlevel, m_nCurrentZoomLevel, new DialogInterface.OnClickListener() {						
				@Override
				public void onClick(DialogInterface dialog, int item) {							
					m_nCurrentZoomLevel = item;
					dialog.dismiss();
					mMapView.setZoomLevel(Integer.parseInt(arrString[item]));					
				}
			}).show();		
    }
    
    /**
     * seetMapType  
     * Map의 Type을 설정한다.
     */
	public void setMapType() {
    	AlertDialog dlg = new AlertDialog.Builder(this)
		.setIcon(R.drawable.ic_launcher)
		.setTitle("Select MAP Type")
		.setSingleChoiceItems(R.array.a_maptype, -1, new DialogInterface.OnClickListener() {						
			@Override
			public void onClick(DialogInterface dialog, int item) {							
				LogManager.printLog("Set Map Type " + item);
				dialog.dismiss();
				mMapView.setMapType(item);
			}
		}).show();		
    }
    
    /**
     * getLocationPoint
     * 현재위치로 표시될 좌표의 위도, 경도를 반환한다. 
     */
	public void getLocationPoint() {
		TMapPoint point = mMapView.getLocationPoint();
		
		double Latitude = point.getLatitude();
		double Longitude = point.getLongitude();
		
		m_Latitude  = Latitude;
		m_Longitude = Longitude;
		
		LogManager.printLog("Latitude " + Latitude + " Longitude " + Longitude);
		
		String strResult = String.format("Latitude = %f Longitude = %f", Latitude, Longitude);
		
		Common.showAlertDialog(this, "", strResult);
	}
	
	/**
	 * setLocationPoint
	 * 현재위치로 표시될 좌표의 위도,경도를 설정한다. 
	 */
	public void setLocationPoint() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	      
	      Location location = locationManager
	                   .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

	      double latitude = location.getLatitude();//((double)Math.random() ) * (37.575113-37.483086) + 37.483086;
	       double longitude = location.getLongitude();
		
		LogManager.printLog("setLocationPoint " + latitude + " " + longitude);
		
		mMapView.setLocationPoint(latitude, longitude);
	}
	
	/**
	 * setMapIcon
	 * 현재위치로 표시될 아이콘을 설정한다. 
	 */
	public void setMapIcon() {
		m_bShowMapIcon = !m_bShowMapIcon;

		if (m_bShowMapIcon) {
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_launcher);
			mMapView.setIcon(bitmap);
		}
		mMapView.setIconVisibility(m_bShowMapIcon);
	}
	
	/**
	 * setCompassMode
	 * 단말의 방항에 따라 움직이는 나침반모드로 설정한다. 
	 */
	public void setCompassMode() {
		mMapView.setCompassMode(!mMapView.getIsCompass());
	}
	
	/**
	 * getIsCompass
	 * 나침반모드의 사용여부를 반환한다. 
	 */
	public void getIsCompass() {
		Boolean bGetIsCompass = mMapView.getIsCompass();
		Common.showAlertDialog(this, "", "현재 나침반 모드는 : " + bGetIsCompass.toString() );
	}
	
	/**
	 * setTrafficeInfo
	 * 실시간 교통정보를 표출여부를 설정한다. 
	 */
	public void setTrafficeInfo() {
		m_bTrafficeMode = !m_bTrafficeMode;
		mMapView.setTrafficInfo(m_bTrafficeMode);
	}
	
	/**
	 * getIsTrafficeInfo
	 * 실시간 교통정보 표출상태를 반환한다. 
	 */
	public void getIsTrafficeInfo() {
		Boolean bIsTrafficeInfo = mMapView.IsTrafficInfo();
		Common.showAlertDialog(this, "", "현재 실시간 교통정보 표출상태는  : " + bIsTrafficeInfo.toString() );
	}
	
	/**
	 * setSightVisible
	 * 시야표출여부를 설정한다. 
	 */
	public void setSightVisible() {
		m_bSightVisible = !m_bSightVisible;
		mMapView.setSightVisible(m_bSightVisible);
	}
	
	/**
	 * setTrackingMode
	 * 화면중심을 단말의 현재위치로 이동시켜주는 트래킹모드로 설정한다. 
	 */
	public void setTrackingMode() {
		m_bTrackingMode = !m_bTrackingMode;
		mMapView.setTrackingMode(m_bTrackingMode);
	}
	
	/**
	 * getIsTracking
	 * 트래킹모드의 사용여부를 반환한다. 
	 */
	public void getIsTracking() {
		Boolean bIsTracking = mMapView.getIsTracking();
		Common.showAlertDialog(this, "", "현재 트래킹모드 사용 여부  : " + bIsTracking.toString() );
	}
	
	/**
	 * addTMapCircle()
	 * 지도에 서클을 추가한다. 
	 */
	public void addTMapCircle() {
		TMapCircle circle = new TMapCircle();
		
		circle.setRadius(300);
		circle.setLineColor(Color.BLUE);
		circle.setAreaAlpha(50);
		circle.setCircleWidth((float)10);
		circle.setRadiusVisible(true);
		
		TMapPoint point = randomTMapPoint();
		circle.setCenterPoint(point);
		
		String strID = String.format("circle%d", mCircleID++);
		mMapView.addTMapCircle(strID, circle);
		
		mArrayCircleID.add(strID);
	}
	
	/**
	 * removeTMapCircle
	 * 지도상의 해당 서클을 제거한다. 
	 */
	public void removeTMapCircle() {
		if(mArrayCircleID.size() <= 0 )
			return;
		
		String strCircleID = mArrayCircleID.get(mArrayCircleID.size() - 1 );
		mMapView.removeTMapCircle(strCircleID);
		
		mArrayCircleID.remove(mArrayCircleID.size() - 1);
		//mMapView.showCallOutViewWithMarkerItemID("02");
	}
	
	public void showMarkerPoint2() {
		ArrayList<Bitmap> list = null;
		for(int i = 0; i < 50; i++) {
			
			MarkerOverlay marker1 = new MarkerOverlay(this, mMapView);
			String strID = String.format("%02d", i);
			
			marker1.setID(strID);
			marker1.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.map_pin_red));		
			marker1.setTMapPoint(randomTMapPoint());
			
			if(list == null){
				 list = new ArrayList<Bitmap>();
			}
			
			list.add(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_pin_red));
			list.add(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.end));
			
			marker1.setAnimationIcons(list);
			//marker1.setAniDuration(1000);
			
			//marker1.startAnimation();
			mMapView.addMarkerItem2(strID, marker1);
			
		}
				
		mMapView.setOnMarkerClickEvent(new TMapView.OnCalloutMarker2ClickCallback() {
			
			@Override
			public void onCalloutMarker2ClickEvent(String id, TMapMarkerItem2 markerItem2) {
				LogManager.printLog("ClickEvent " + " id " + id + " " + markerItem2.latitude + " " +  markerItem2.longitude);
				
				String strMessage = "ClickEvent " + " id " + id + " " + markerItem2.latitude + " " +  markerItem2.longitude;
				
				Common.showAlertDialog(MainActivity.this, "TMapMarker2", strMessage);
			}
		});
		
	}
	
	/**
	 * showMarkerPoint
	 * 지도에 마커를 표출한다. 
	 */
	public void showMarkerPoint()
	{	

		Bitmap bitmap = null;
		
		TMapPoint point = new TMapPoint(37.566474, 126.985022);
				
		TMapMarkerItem item1 = new TMapMarkerItem();
		
		bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.i_location);
				
		item1.setTMapPoint(point);
		item1.setName("SKT타워");
		item1.setVisible(item1.VISIBLE);
	
		item1.setIcon(bitmap);
		LogManager.printLog("bitmap " + bitmap.getWidth() + " " + bitmap.getHeight());
		
		bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.i_location);		
		item1.setCalloutTitle("SKT타워");
		item1.setCalloutSubTitle("을지로입구역 500M");
		item1.setCanShowCallout(true);
		item1.setAutoCalloutVisible(true);
		
		Bitmap bitmap_i = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.i_go);
		
		
		
		//item1.setCalloutLeftImage(bitmap);
		item1.setCalloutRightButtonImage(bitmap_i);

		String strID = String.format("pmarker%d", mMarkerID++);
		
		mMapView.addMarkerItem(strID, item1);
		mArrayMarkerID.add(strID);
		
		
		point = new TMapPoint(37.55102510077652, 126.98789834976196);
		TMapMarkerItem item2 = new TMapMarkerItem();

		item2.setTMapPoint(point);
		item2.setName("N서울타워");
		item2.setVisible(item2.VISIBLE);
		item2.setCalloutTitle("청호타워 4층");
		
		//item2.setCalloutSubTitle("을지로입구역 500M");
		
		item2.setCanShowCallout(true);
		
		
		//item2.setAutoCalloutVisible(true);
		
		//item2.setCalloutLeftImage(bitmap);
		
		bitmap_i = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.i_go);		
		item2.setCalloutRightButtonImage(bitmap_i);
				
		bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.pin_tevent);
		item2.setIcon(bitmap);

		strID = String.format("pmarker%d", mMarkerID++);
		
		mMapView.addMarkerItem(strID, item2);
		mArrayMarkerID.add(strID);
		
		
		point = new TMapPoint(37.58102510077652, 126.98789834976196);
		item2 = new TMapMarkerItem();

		item2.setTMapPoint(point);
		item2.setName("N서울타워");
		item2.setVisible(item2.VISIBLE);
		item2.setCalloutTitle("창덕궁 청호타워 4층");
		
		item2.setCalloutSubTitle("을지로입구역 500M");
		item2.setCanShowCallout(true);
		
			
		bitmap_i = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.i_go);		
		item2.setCalloutRightButtonImage(bitmap_i);
				
		bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.map_pin_red);
		item2.setIcon(bitmap);

		strID = String.format("pmarker%d", mMarkerID++);
		
		mMapView.addMarkerItem(strID, item2);
		mArrayMarkerID.add(strID);
				
		point = new TMapPoint(37.58102510077652, 126.99789834976196);
		item2 = new TMapMarkerItem();

		item2.setTMapPoint(point);
		item2.setName("N서울타워");
		item2.setVisible(item2.VISIBLE);
		item2.setCalloutTitle("대학로 혜화역111111");
				
		item2.setCanShowCallout(true);
				
		item2.setCalloutLeftImage(bitmap);
		
		bitmap_i = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.i_go);		
		item2.setCalloutRightButtonImage(bitmap_i);
				
		
		bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.end);
		item2.setIcon(bitmap);

		strID = String.format("pmarker%d", mMarkerID++);
		
		mMapView.addMarkerItem(strID, item2);
		mArrayMarkerID.add(strID);
	
		for(int i = 4; i < 10; i++) {
			TMapMarkerItem item3 = new TMapMarkerItem();
			
			item3.setID(strID);
			item3.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.map_pin_red));

			item3.setVisible(item3.VISIBLE);
			item3.setTMapPoint(randomTMapPoint());
			item3.setCalloutTitle(">>>>" + strID + "<<<<<");
			item3.setCanShowCallout(true);
			
			//marker1.startAnimation();

			strID = String.format("pmarker%d", mMarkerID++);
			
			mMapView.addMarkerItem(strID, item2);
			mArrayMarkerID.add(strID);
			
		}
	}
	
		
	
	public void removeMarker()
	{
		if(mArrayMarkerID.size() <= 0 )
			return;
		
		String strMarkerID = mArrayMarkerID.get(mArrayMarkerID.size() - 1);
		mMapView.removeMarkerItem(strMarkerID);
		
		mArrayMarkerID.remove(mArrayMarkerID.size() - 1);
		
	}
	
	
	/**
	 * moveFrontMarker
	 * 마커를 맨 앞으로 표시 하도록 한다. 
	 * showMarkerPoint() 함수를 먼저 클릭을 한 후, 클릭을 해야 함.
	 */
	public void moveFrontMarker()
	{
		TMapMarkerItem item = mMapView.getMarkerItemFromID("1");
		
		mMapView.bringMarkerToFront(item);
	}
	
	
	/**
	 * moveBackMarker
	 * 마커를 맨 뒤에 표시하도록 한다. 
	 * showMarkerPoint() 함수를 먼저 클릭을 한 후, 클릭을 해야 함.
	 */
	public void moveBackMarker()
	{
		TMapMarkerItem item = mMapView.getMarkerItemFromID("1");
		
		mMapView.sendMarkerToBack(item);
	}
	
	
	/**
	 * drawLine
	 * 지도에 라인을 추가한다. 
	 */
	public void drawLine()
	{	
		TMapPolyLine polyLine = new TMapPolyLine();
		polyLine.setLineColor(Color.BLUE);
		polyLine.setLineWidth(5);
		
		for(int i = 0; i < 5; i++)
		{
			TMapPoint point = randomTMapPoint();
			polyLine.addLinePoint(point);
		}
		
		String strID = String.format("line%d", mLineID++);
		
		mMapView.addTMapPolyLine(strID, polyLine);
		
		mArrayLineID.add(strID);
		
	}
	
	
	/**
	 * erasePolyLine
	 * 지도에 라인을 제거한다. 
	 */
	public void erasePolyLine()
	{
		if(mArrayLineID.size() <= 0)
			return;
		
		String strLineID = mArrayLineID.get(mArrayLineID.size() - 1 );
		mMapView.removeTMapPolyLine(strLineID);
		
		mArrayLineID.remove(mArrayLineID.size() - 1);
		
	}

	
	
	/**
	 * drawPolygon
	 * 지도에 폴리곤에 그린다. 
	 */
	public void drawPolygon()
	{			
		int Min = 3;
		int Max = 10;
		int rndNum = (int)(Math.random() * ( Max - Min ));
		
		LogManager.printLog("drawPolygon" + rndNum);
		
		TMapPolygon polygon = new TMapPolygon();
		polygon.setLineColor(Color.BLUE);
		polygon.setPolygonWidth((float)4);
		polygon.setAreaAlpha(2);
		    
		TMapPoint point = null;
		
		if(rndNum < 3 )
		{
			rndNum = rndNum + (3 - rndNum);
		}
		
		for(int i = 0; i < rndNum; i++)
		{
			point = randomTMapPoint(); 
			polygon.addPolygonPoint(point);
			
		}
				
		String strID = String.format("polygon%d", mPolygonID++);
		mMapView.addTMapPolygon(strID, polygon);
		
		mArrayPolygonID.add(strID);
		
	}
	
	/**
	 * erasePolygon
	 * 지도에 그려진 폴리곤을 제거한다. 
	 */
	public void erasePolygon()
	{	
		if(mArrayPolygonID.size() <= 0)
			return;
		
		String strPolygonID = mArrayPolygonID.get(mArrayPolygonID.size() - 1 );
		
		LogManager.printLog("erasePolygon " + strPolygonID);
		
		mMapView.removeTMapPolygon(strPolygonID);
		
		mArrayPolygonID.remove(mArrayPolygonID.size() - 1);
		
	}
	
	
	
	/**
	 * drawMapPath
	 * 지도에 시작-종료 점에 대해서 경로를 표시한다. 
	 */
	public void drawMapPath()
	{			
		TMapPoint point1 = mMapView.getCenterPoint();
		TMapPoint point2 = randomTMapPoint();
		
		TMapData tmapdata = new TMapData();
			
		tmapdata.findPathData(point1, point2, new FindPathDataListenerCallback() {
			
			@Override
			public void onFindPathData(TMapPolyLine polyLine) {

				mMapView.addTMapPath(polyLine);
			}
		});
		
	}
	
	
	private String getContentFromNode(Element item, String tagName){
		NodeList list = item.getElementsByTagName(tagName);
		if(list.getLength() > 0){
			if(list.item(0).getFirstChild() != null)
			{
				return list.item(0).getFirstChild().getNodeValue();
			}
		}
		return null;
	}
	
	
	
	/**
	 * displayMapInfo()
	 * POI들이 모두 표시될 수 있는 줌레벨 결정함수와 중심점리턴하는 함수
	 */
	public void displayMapInfo()
	{	
		/*
		TMapPoint point1 = mMapView.getCenterPoint();		
		TMapPoint point2 = randomTMapPoint();
		*/
		TMapPoint point1 = new TMapPoint(37.541642248630524, 126.99599611759186);
		
		TMapPoint point2 = new TMapPoint(37.541243493556976, 126.99659830331802);
		
		TMapPoint point3 = new TMapPoint(37.540909826755524, 126.99739581346512);
		
		TMapPoint point4 = new TMapPoint(37.541080713272095, 126.99874675273895);
					
		ArrayList<TMapPoint> point = new ArrayList<TMapPoint>();
		
		point.add(point1);
		point.add(point2);
		point.add(point3);
		point.add(point4);
		
		
		TMapInfo info = mMapView.getDisplayTMapInfo(point);
		
		String strInfo = "Center Latitude" + info.getTMapPoint().getLatitude() + "Center Longitude" + info.getTMapPoint().getLongitude() + 
						"Level " + info.getTMapZoomLevel();
		
		Common.showAlertDialog(this, "", strInfo );
		
		
	}
	
	
	/**
	 * removeMapPath
	 * 경로 표시를 삭제한다. 
	 */
	public void removeMapPath()
	{	
		mMapView.removeTMapPath();
		
	}
	
	
	
	/**
	 * naviGuide
	 * 길안내 
	 */
	public void naviGuide()
	{			
		TMapPoint point1 = mMapView.getCenterPoint();
		TMapPoint point2 = randomTMapPoint();
		
		TMapData tmapdata = new TMapData();
		
		tmapdata.findPathDataAll(point1, point2, new FindPathDataAllListenerCallback() {
			
			@Override
			public void onFindPathDataAll(Document doc) {
				
			
			}
		});
		
		
	}
	
	
	public void drawCarPath()
	{	
		
		TMapPoint point1 = mMapView.getCenterPoint();
		TMapPoint point2 = randomTMapPoint();
		
		TMapData tmapdata = new TMapData();
		
		tmapdata.findPathDataWithType(TMapPathType.CAR_PATH, point1, point2, new FindPathDataListenerCallback() {
			
			@Override
			public void onFindPathData(TMapPolyLine polyLine) {
				
				mMapView.addTMapPath(polyLine);
				
			}
		});
		
				
	}
	
	
	
	public void  drawPedestrianPath()
	{				
		TMapPoint point1 = mMapView.getCenterPoint();
		TMapPoint point2 = randomTMapPoint();
		
		TMapData tmapdata = new TMapData();
		
		tmapdata.findPathDataWithType(TMapPathType.PEDESTRIAN_PATH, point1, point2, new FindPathDataListenerCallback() {
			
			@Override
			public void onFindPathData(TMapPolyLine polyLine) {
				
				polyLine.setLineColor(Color.BLUE);
				mMapView.addTMapPath(polyLine);
				
			}
		});
		
		
	}
	
	
	
	public void drawBicyclePath()
	{		
		TMapPoint point1 = mMapView.getCenterPoint();
		TMapPoint point2 = randomTMapPoint();
		
		TMapData tmapdata = new TMapData();
		
		
		tmapdata.findPathDataWithType(TMapPathType.BICYCLE_PATH, point1, point2, new FindPathDataListenerCallback() {
			
			@Override
			public void onFindPathData(TMapPolyLine polyLine) {
				
				mMapView.addTMapPath(polyLine);
				
			}
		});
		
		
	}
	
	
	
	
	
	
	
	/**
	 * getCenterPoint
	 * 지도의 중심점을 가지고 온다. 
	 */
	public void getCenterPoint()
	{
		TMapPoint point = mMapView.getCenterPoint();
		
		Common.showAlertDialog(this, "", "지도의 중심 좌표는 " + point.getLatitude() + " " + point.getLongitude() );
		
	}
	
	
	/**
	 * findAllPoi
	 * 통합검색 POI를 요청한다. 
	 */
	public void findAllPoi()
	{		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("POI 통합 검색");

		final EditText input = new EditText(this);
		builder.setView(input);

		builder.setPositiveButton("확인", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    			    		    	
		    	final String strData = input.getText().toString();
		        
			    TMapData tmapdata = new TMapData();
			      
			    tmapdata.findAllPOI(strData, new FindAllPOIListenerCallback() {
					
					@Override
					public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
						
						for(int i = 0; i < poiItem.size(); i++)
			        	{
			        		TMapPOIItem  item = poiItem.get(i);
			        				        		        
			        		LogManager.printLog("POI Name: " + item.getPOIName().toString() + ", " + 
			        				            "Address: " + item.getPOIAddress().replace("null", "")  + ", " + 
			        				            "Point: " + item.getPOIPoint().toString());
			        		
			        	}
						
					}
				});
			    
			    
		    }
		    
		});
		builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		builder.show();		

	}
	
	
	/**
	 * convertToAddress
	 * 지도에서 선택한 지점을 주소를 변경요청한다. 
	 */
	public void convertToAddress()
	{			
		TMapPoint point = mMapView.getCenterPoint();
		
	    TMapData tmapdata = new TMapData();
	      
	    if(mMapView.isValidTMapPoint(point))
	    {
		    tmapdata.convertGpsToAddress(point.getLatitude(), point.getLongitude(), new ConvertGPSToAddressListenerCallback() {
				
				@Override
				public void onConvertToGPSToAddress(String strAddress) {
					
					LogManager.printLog("선택한 위치의 주소는 " + strAddress);
				}
			});
//		    tmapdata.reverseGeocoding(point.getLatitude(), point.getLongitude(), "A03", new reverseGeocodingListenerCallback() {
//				
//				@Override
//				public void onReverseGeocoding(TMapAddressInfo addressInfo) {
//					
//					LogManager.printLog("선택한 위치의 주소는 " + addressInfo.strFullAddress);
//				}
//			});
//		    tmapdata.geoCodingWithAddressType("F02", "서울시", "구로구", "새말로", "6", "", new GeoCodingWithAddressTypeListenerCallback() {
//		    	
//				@Override
//				public void onGeoCodingWithAddressType(TMapGeocodingInfo geocodingInfo) {
//					LogManager.printLog(">>> strMatchFlag : " + geocodingInfo.strMatchFlag);
//					LogManager.printLog(">>> strLatitude : " + geocodingInfo.strLatitude);
//					LogManager.printLog(">>> strLongitude : " + geocodingInfo.strLongitude);
//					LogManager.printLog(">>> strCity_do : " + geocodingInfo.strCity_do);
//					LogManager.printLog(">>> strGu_gun : " + geocodingInfo.strGu_gun);
//					LogManager.printLog(">>> strLegalDong : " + geocodingInfo.strLegalDong);
//					LogManager.printLog(">>> strAdminDong : " + geocodingInfo.strAdminDong);
//					LogManager.printLog(">>> strBunji : " + geocodingInfo.strBunji);
//					LogManager.printLog(">>> strNewMatchFlag : " + geocodingInfo.strNewMatchFlag);
//					LogManager.printLog(">>> strNewLatitude : " + geocodingInfo.strNewLatitude);
//					LogManager.printLog(">>> strNewLongitude : " + geocodingInfo.strNewLongitude);
//					LogManager.printLog(">>> strNewRoadName : " + geocodingInfo.strNewRoadName);
//					LogManager.printLog(">>> strNewBuildingIndex : " + geocodingInfo.strNewBuildingIndex);
//					LogManager.printLog(">>> strNewBuildingName : " + geocodingInfo.strNewBuildingName);
//				}
//			});
	    }
		  
	}    
	    
	
	/**
	 * getBizCategory
	 * 업종별 category를 요청한다. 
	 */
	public void getBizCategory()
	{		
		TMapData tmapdata = new TMapData();
		
        tmapdata.getBizCategory(new BizCategoryListenerCallback() {
			
			@Override
			public void onGetBizCategory(ArrayList<BizCategory> poiItem) {
				
				for(int i = 0; i < poiItem.size(); i++)
		        {
		        	BizCategory item = poiItem.get(i);
		        	
		        	LogManager.printLog("UpperBizCode " + item.upperBizCode + " " + "UpperBizName " + item.upperBizName );
		        	LogManager.printLog("MiddleBizcode " + item.middleBizCode + " " + "MiddleBizName " + item.middleBizName);
		        }
			}
		});
            
	}
	
	
	
	/**
	 * getAroundBizPoi
	 * 업종별 주변검색 POI 데이터를 요청한다. 
	 */
	public void getAroundBizPoi()
	{				
		TMapData tmapdata = new TMapData();
		 
		TMapPoint point = mMapView.getCenterPoint();
		
		
		/*
		tmapdata.findAroundBizPOI(point, "01", "편의점", new FindAroundBizPOIListenerCallback() {
			
			@Override
			public void onFindAroundBizPOI(ArrayList<TMapPOIItem> poiItem) {
				
				for(int i = 0; i < poiItem.size(); i++)
	            {
	            	TMapPOIItem item = poiItem.get(i);
	            	
	            	LogManager.printLog("POI Name: " + item.getPOIName() + "," + 
	            						"Address: " + item.getPOIAddress().replace("null", ""));
	            
	            }
				
			}
		});
		*/		
		
		
		
		tmapdata.findAroundNamePOI(point, "편의점;은행",1, 99, new FindAroundNamePOIListenerCallback() {
			
			@Override
			public void onFindAroundNamePOI(ArrayList<TMapPOIItem> poiItem) {
				
				for(int i = 0; i < poiItem.size(); i++)
	            {
					TMapPOIItem item = poiItem.get(i);
	            	
	            	LogManager.printLog("POI Name: " + item.getPOIName() + "," + 
	            						"Address: " + item.getPOIAddress().replace("null", ""));
	            }
				
			}
		});
		
		
		
	}
	
	
	public void setTileType() {
    	AlertDialog dlg = new AlertDialog.Builder(this)
		.setIcon(R.drawable.ic_launcher)
		.setTitle("Select MAP Tile Type")
		.setSingleChoiceItems(R.array.a_tiletype, -1, new DialogInterface.OnClickListener() {						
			@Override
			public void onClick(DialogInterface dialog, int item) {							
				LogManager.printLog("Set Map Tile Type " + item);
				dialog.dismiss();
				mMapView.setTileType(item);
			}
		}).show();	
	}
	
	public void setBicycle()
	{
		mMapView.setBicycleInfo(!mMapView.IsBicycleInfo());
	}	
	
	public void setBicycleFacility()
	{
		mMapView.setBicycleFacilityInfo(!mMapView.isBicycleFacilityInfo());
	}
	
	
	public void invokeRoute()
	{	

		final TMapPoint point = mMapView.getCenterPoint();
		TMapData tmapdata = new TMapData();
				
		if(mMapView.isValidTMapPoint(point)) {
			
			tmapdata.convertGpsToAddress(point.getLatitude(), point.getLongitude(), new ConvertGPSToAddressListenerCallback() {
				
				@Override
				public void onConvertToGPSToAddress(String strAddress) {
					
					TMapTapi tmaptapi = new TMapTapi(MainActivity.this);
									
					float fY = (float)point.getLatitude();
					float fX = (float)point.getLongitude();
					
					tmaptapi.invokeRoute(strAddress, fX, fY);
					
				}
			});
		}
	
	}
	
	
	public void invokeSetLocation()
	{				
		final TMapPoint point = mMapView.getCenterPoint();
		TMapData tmapdata = new TMapData();
		
		
		tmapdata.convertGpsToAddress(point.getLatitude(), point.getLongitude(), new ConvertGPSToAddressListenerCallback() {
			
			@Override
			public void onConvertToGPSToAddress(String strAddress) {
				
				TMapTapi tmaptapi = new TMapTapi(MainActivity.this);
				
				float fY = (float)point.getLatitude();
				float fX = (float)point.getLongitude();
				
				tmaptapi.invokeSetLocation(strAddress, fX, fY);
				
			}
		});
		
		
	}
	
	
	
	String strSearch = "";
	
	public void invokeSearchProtal()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("T MAP 통합 검색");

		final EditText input = new EditText(this);
		builder.setView(input);

		builder.setPositiveButton("확인", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        
		    	strSearch = input.getText().toString();
		        		    	
		    	new Thread()
				{
					@Override
					public void run()
					{	
						TMapTapi tmaptapi = new TMapTapi(MainActivity.this);
												
						if(strSearch.trim().length() > 0)
							tmaptapi.invokeSearchPortal(strSearch);						
					}
					
				}.start();
		        
		    }
		});
		builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		builder.show();
		
	}
	
	
	public void tmapInstall()
	{
    	new Thread()
		{
			@Override
			public void run()
			{	
				TMapTapi tmaptapi = new TMapTapi(MainActivity.this);
		        Uri uri = Uri.parse(tmaptapi.getTMapDownUrl().get(0));
		        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		        startActivity(intent);					
			}
			
		}.start();

	}

	
	public void captureImage()
	{	
		/*
		Bitmap bitmap = mMapView.getCaptureImage();
		
		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		 
		File path = new File(sdcard + File.separator + "image_write");
	    if (!path.exists()) 
		   path.mkdir();
	    
	    File fileCacheItem = new File(path.toString() + File.separator + System.currentTimeMillis() + ".png");
        OutputStream out = null;
        
        
        try
        {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
 
            bitmap.compress(CompressFormat.JPEG, 90, out);
            
            out.flush();
            out.close();  
        }catch (Exception e) {
            e.printStackTrace();
        }
        */
		
		
		mMapView.getCaptureImage(20, new MapCaptureImageListenerCallback() {
			
			@Override
			public void onMapCaptureImage(Bitmap bitmap) {
				
				String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
				 
				File path = new File(sdcard + File.separator + "image_write");
			    if (!path.exists()) 
				   path.mkdir();
			    
			    File fileCacheItem = new File(path.toString() + File.separator + System.currentTimeMillis() + ".png");
		        OutputStream out = null;
		        
		        try
		        {
		            fileCacheItem.createNewFile();
		            out = new FileOutputStream(fileCacheItem);
		 
		            bitmap.compress(CompressFormat.JPEG, 90, out);
		            
		            out.flush();
		            out.close();  
		        }
		        catch (Exception e) 
		        {
		            e.printStackTrace();
		        }
				
			}
		});
        
        
	}
	
	
	private boolean bZoomEnable = false;
	
	public void disableZoom()
	{
		bZoomEnable = !bZoomEnable;
		mMapView.setUserScrollZoomEnable(bZoomEnable);
	}
	
	
	
	public void timeMachine() 
	{
		TMapData tmapdata = new TMapData();
		
		HashMap<String, String> pathInfo = new HashMap<String, String>();
		
		pathInfo.put("rStName", "T Tower");

		pathInfo.put("rStlat", Double.toString(37.566474));
		pathInfo.put("rStlon", Double.toString(126.985022));
		
		pathInfo.put("rGoName", "신도림");
		pathInfo.put("rGolat", "37.50861147");
		pathInfo.put("rGolon", "126.8911457");
		
		pathInfo.put("type", "arrival");
		
		Date currentTime = new Date();
		
		tmapdata.findTimeMachineCarPath(pathInfo,  currentTime, null);	
		
	}

}


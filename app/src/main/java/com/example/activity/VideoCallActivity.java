package com.example.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.example.CustomXML.IceCandidateExtensionElement;
import com.example.CustomXML.SDPExtensionElement;
import com.example.CustomXML.VideoInvitation;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VideoCallActivity extends ParentActivity{
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String LOCAL_MEDIA_STREAM_ID = "ARDAMS";
    private String mServiceName;    //XMPP服务器名称

    private GLSurfaceView mGLSurfaceView;
//    private GLSurfaceView mGLSurfaceViewRemote;

    private PeerConnection pc;
    private final PCObserver pcObserver = new PCObserver();
    private final SDPObserver sdpObserver = new SDPObserver();

    private MediaConstraints sdpMediaConstraints;
    private MediaConstraints pcConstraints;
    private String remoteName;
    IceCandidate remoteIceCandidate;

    private boolean mIsInited;
    private boolean mIsCalled;
    PeerConnectionFactory factory;
    VideoCapturer videoCapturer;
    VideoSource videoSource;

    VideoRenderer localVideoRenderer;
    VideoRenderer remoteVideoRenderer;

    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        //打开扬声器
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);

        mServiceName = connection.getServiceName();
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
//        mGLSurfaceViewRemote = (GLSurfaceView) findViewById(R.id.glsurfaceview_remote);
        //检查初始化音视频设备是否成功
        if (!PeerConnectionFactory.initializeAndroidGlobals(this,true,true,true,null))
        {
            Log.e("init","PeerConnectionFactory init fail!");
            return;
        }

//        Intent intent = getIntent().getBundleExtra()

        //Media条件信息SDP接口
        sdpMediaConstraints = new MediaConstraints();
        //接受远程音频
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveAudio", "true"));
        //接受远程视频
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"));
//        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation","true"));
//        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl","true"));
//        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseReduction","true"));

        factory = new PeerConnectionFactory();

        //iceServer List对象获取
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        pcConstraints = new MediaConstraints();
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair(
                "DtlsSrtpKeyAgreement", "true"));
        pcConstraints.mandatory.add(new
                MediaConstraints.KeyValuePair("VoiceActivityDetection", "false"));
//        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation","true"));
//        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl","true"));
//        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseReduction","true"));

        pc = factory.createPeerConnection(iceServers,pcConstraints,pcObserver);


        mIsInited = false;
        mIsCalled=false;

        boolean offer=getIntent().getBooleanExtra("createOffer",false);
        //offer：如果offer为true表示主叫方初始化，如果为false表示被叫方初始化。
        remoteName = getIntent().getStringExtra("remoteName");
        if(!offer)
        {
            initialSystem();
        }
        else {
            callRemote(remoteName);
        }
        //当VideoActivity已经打开时，处理后续的intent传过来的数据。
        processExtraData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //通话结束，发送通话结束消息
        videoCallEnded();
        //释放资源
        videoCapturer.dispose();
        videoSource.stop();
        if (pc != null) {
            pc.dispose();
            pc = null;
        }

        audioManager.setSpeakerphoneOn(false);
    }

    private void videoCallEnded() {
        String chatJid = remoteName+"@"+mServiceName;
        Message message = new Message();
        VideoInvitation videoInvitation = new VideoInvitation();
        videoInvitation.setTypeText("video-ended");
        message.addExtension(videoInvitation);
        Chat chat = createChat(chatJid);
        try {
            chat.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void processExtraData() {
        Intent intent = getIntent();
        //获取SDP数据
        String sdpType = intent.getStringExtra("type");
        String sdpDescription = intent.getStringExtra("description");
        if (sdpType != null)
        {

            SessionDescription.Type type = SessionDescription.Type.fromCanonicalForm(sdpType);
            SessionDescription sdp = new SessionDescription(type,sdpDescription);
            if (pc == null)
            {
                Log.e("pc","pc == null");
            }
            pc.setRemoteDescription(sdpObserver,sdp);

            //如果是offer,则被叫方createAnswer
            if (sdpType.equals("offer"))
            {
                mIsCalled = true;
                pc.createAnswer(sdpObserver,sdpMediaConstraints);
            }

        }

        //获取ICE Candidate数据
        String iceSdpMid = intent.getStringExtra("sdpMid");
        int iceSdpMLineIndex = intent.getIntExtra("sdpMLineIndex",-1);
        String iceSdp = intent.getStringExtra("sdp");
        if (iceSdpMid != null)
        {
            IceCandidate iceCandidate = new IceCandidate(iceSdpMid,iceSdpMLineIndex,iceSdp);

            if (remoteIceCandidate == null)
            {
                remoteIceCandidate = iceCandidate;
            }

            //下面这步放到函数drainRemoteCandidates()中
            /*//添加远端的IceCandidate到pc
            pc.addIceCandidate(iceCandidate);*/
        }


        //结束activity
        boolean videoEnded = intent.getBooleanExtra("videoEnded",false);
        if (videoEnded)
        {
            finish();
        }
    }

 /*   //chatJid: friendUsername@serviceName
    private Chat createChat(String chatJid)
    {
        if (isConnected())
        {
            ChatManager chatManager = ChatManager.getInstanceFor(connection);
            return chatManager.createChat(chatJid);
        }
        throw new NullPointerException("连接服务器失败，请先连接服务器!");
    }*/

    private void callRemote(String remoteName) {
        initialSystem();
        //createOffer
        pc.createOffer(sdpObserver,sdpMediaConstraints);
    }

    private void initialSystem() {
        if (mIsInited)
        {
            return;
        }
        //获取前置摄像头本地视频流
        String frontDeviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
//        String frontDeviceName = "Camera 1, Facing front, Orientation 0";
        Log.e("CameraName","CameraName: "+frontDeviceName);
        videoCapturer = VideoCapturerAndroid.create(frontDeviceName);

      /*  //获取后置摄像头本地视频流
        String backDeviceName = VideoCapturerAndroid.getNameOfBackFacingDevice();
        Log.e("CameraName","CameraName: "+backDeviceName);
        videoCapturer = VideoCapturerAndroid.create(backDeviceName);*/

        /*int cameraNums = VideoCapturerAndroid.getDeviceCount();
        Log.e("CameraCount","CameraCount: "+cameraNums);*/

    /*    //手机上直接使用上面注释代码指定前置和后置摄像头就行，当外接USB摄像头时，
        //使用下面代码获取USB摄像头。
        String[] cameraNames = VideoCapturerAndroid.getDeviceNames();
        for (String cameraName : cameraNames)
        {
            videoCapturer = VideoCapturerAndroid.create(cameraName);
            if (videoCapturer != null)
            {
                break;
            }
        }*/

        if (videoCapturer == null)
        {
            Log.e("open","fail to open camera");
            return;
        }
        //视频
        MediaConstraints mediaConstraints = new MediaConstraints();
        videoSource = factory.createVideoSource(videoCapturer, mediaConstraints);
        VideoTrack localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);

        //音频
        MediaConstraints audioConstraints = new MediaConstraints();
        AudioSource audioSource = factory.createAudioSource(audioConstraints);
        AudioTrack localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID,audioSource);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };
        VideoRendererGui.setView(mGLSurfaceView,runnable);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）

        try {
            //改成ScalingType.SCALE_ASPECT_FILL可以显示双方视频，但是显示比例不美观，并且不知道最后一个参数true和false的含义。
            localVideoRenderer = VideoRendererGui.createGui(0,0,100,100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL,true);
            remoteVideoRenderer = VideoRendererGui.createGui(0,0,100,100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL,true);
            localVideoTrack.addRenderer(localVideoRenderer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaStream localMediaStream = factory.createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);

        localMediaStream.addTrack(localAudioTrack);
        localMediaStream.addTrack(localVideoTrack);

        pc.addStream(localMediaStream);
    }

    private List<PeerConnection.IceServer> getIceServers(String url,String user,String credential)
    {
        PeerConnection.IceServer turn = new PeerConnection.IceServer(
                url,user,credential);
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
        iceServers.add(turn);
        return iceServers;
    }

    private class PCObserver implements PeerConnection.Observer
    {

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        //发送ICE候选到其他客户端
        @Override
        public void onIceCandidate(final IceCandidate iceCandidate) {
            //利用XMPP发送iceCandidate到其他客户端
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String chatJid = remoteName+"@"+mServiceName;
                    Message message = new Message();
                    IceCandidateExtensionElement iceCandidateExtensionElement=
                            new IceCandidateExtensionElement();
                    iceCandidateExtensionElement.setSdpMidText(iceCandidate.sdpMid);
                    iceCandidateExtensionElement.setSdpMLineIndexText(iceCandidate.sdpMLineIndex);
                    iceCandidateExtensionElement.setSdpText(iceCandidate.sdp);
                    message.addExtension(iceCandidateExtensionElement);
                    Chat chat = createChat(chatJid);
                    try {
                        chat.sendMessage(message);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        //Display a media stream from remote
        @Override
        public void onAddStream(final MediaStream mediaStream) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pc == null)
                    {
                        Log.e("onAddStream","pc == null");
                        return;
                    }
                    if (mediaStream.videoTracks.size()>1 || mediaStream.audioTracks.size()>1)
                    {
                        Log.e("onAddStream","size > 1");
                        return;
                    }
                    if (mediaStream.videoTracks.size() == 1)
                    {
                       /* Log.e("addStream","onAddStream() onStart");
                        Log.e("mediaStream","mediaStream: "+mediaStream.toString());
                        Log.e("streamSize","streamSize: "+mediaStream.videoTracks.size());*/
                        VideoTrack videoTrack = mediaStream.videoTracks.get(0);
                        videoTrack.addRenderer(remoteVideoRenderer);
                    }
                }
            });
        }

        @Override
        public void onRemoveStream(final MediaStream mediaStream) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mediaStream.videoTracks.get(0).dispose();
                }
            });
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }
    }

    private class SDPObserver implements SdpObserver
    {

        @Override
        public void onCreateSuccess(final SessionDescription sessionDescription) {
            //sendMessage(offer);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String chatJid = remoteName+"@"+mServiceName;
                    Message message = new Message();
                    SDPExtensionElement sdpExtensionElement = new SDPExtensionElement();
                    sdpExtensionElement.setTypeText(sessionDescription.type.canonicalForm());
                    sdpExtensionElement.setDescriptionText(sessionDescription.description);
                    message.addExtension(sdpExtensionElement);
                    Chat chat = createChat(chatJid);
                    try {
                        chat.sendMessage(message);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }

                    pc.setLocalDescription(sdpObserver,sessionDescription);
                }
            });
        }

        @Override
        public void onSetSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //主叫方
                    if (!mIsCalled)
                    {
                        if (pc.getRemoteDescription() != null)
                        {
                            drainRemoteCandidates();
                        }
                    }
                    //被叫方
                    else
                    {
                        //如果被叫方还没有createAnswer
                        if (pc.getLocalDescription() == null)
                        {
                            Log.e("SDPObserver", "SDPObserver create answer");
                        }
                        else
                        {
                            drainRemoteCandidates();
                        }
                    }
                }
            });
        }

        private void drainRemoteCandidates() {
            if (remoteIceCandidate == null)
            {
                Log.e("SDPObserver","remoteIceCandidate == null");
                return;
            }
            pc.addIceCandidate(remoteIceCandidate);
            Log.e("IceCanditate","添加IceCandidate成功");
            remoteIceCandidate = null;
        }

        @Override
        public void onCreateFailure(String s) {
            Log.e("SDPObserver","onCreateFailure");
        }

        @Override
        public void onSetFailure(String s) {
            Log.e("SDPObserver","onSetFailure");
        }
    }
}

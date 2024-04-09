package model;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

import Service.*;
import com.jcraft.jsch.JSchException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

public class Deps {
	private InterlockingService interlockingService;
	private ScreenService screenService;
	private TrackService trackService;

	private RouteService routeService;
	private CoordinateService coordinateService;

	public BerthService getBerthService() {
		return berthService;
	}

	public void setBerthService(BerthService berthService) {
		this.berthService = berthService;
	}

	private BerthService berthService;

	public SignalService getSignalService() {
		return signalService;
	}


	private SignalService signalService;
	private ViewService viewService;

	public PointService getPointService() {
		return pointService;
	}

	public void setPointService(PointService pointService) {
		this.pointService = pointService;
	}

	private PointService pointService;

	public InterlockingService getInterlockingService() {
		return interlockingService;
	}

	public void setInterlockingService(InterlockingService interlockingService) {
		this.interlockingService = interlockingService;
	}

	public ScreenService getScreenService() {
		return screenService;
	}

	public void setScreenService(ScreenService screenService) {
		this.screenService = screenService;
	}

	public TrackService getTrackService() {
		return trackService;
	}

	public void setTrackService(TrackService trackService) {
		this.trackService = trackService;
	}

	public ViewService getViewService() {
		return viewService;
	}

	public void setViewService(ViewService viewService) {
		this.viewService = viewService;
	}
	public RouteService getRouteService() {
		return routeService;
	}

	public void setRouteService(RouteService routeService) {
		this.routeService = routeService;
	}

	public Deps() throws ParserConfigurationException, IOException, SAXException, InterruptedException, JSchException, AWTException {
		interlockingService = InterlockingService.getInstance();
		screenService  = ScreenService.getInstance();
		trackService = TrackService.getInstance();
		viewService = ViewService.getInstance();
		pointService = PointService.getInstance();
		signalService = SignalService.getInstance();
		coordinateService = CoordinateService.getInstance();
		routeService = RouteService.getInstance();
		berthService= BerthService.getInstance();
	}
	
	public void setup() throws SQLException {
	}

}

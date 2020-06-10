package controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.CollectedInfo;
import model.Product;
import model.Tuple;
import network.EventType;
import network.NetworkManager;
import network.Protocol;
import network.ProtocolType;
import network.Response;
import network.ResponseType;
import utility.IOHandler;

public class SearchPageController implements Initializable {

    @FXML
    private Button SearchBtn;
    @FXML
    private TextField searchField;
    @FXML
    private Button testBtn;
    @FXML
    private TableView<Data> table;
    @FXML
    private TableColumn<Data, ImageView> ImageColumn;

    @FXML
    private TableColumn<Data, String> ProductNameColumn;

    @FXML
    private TableColumn<Data, String> PriceColumn;
    
    @FXML
    private Button goToMainBtn;

    @FXML
    void OnGoToMainBtnClicked(ActionEvent event) {    	
    	try {
   	    	// 메인페이지 엶.
            FXMLLoader.load(getClass().getResource("/page/MainPage.fxml"));
			// 기존 페이지 종료
   	    	Stage nowStage = (Stage) SearchBtn.getScene().getWindow();
   	    	nowStage.close();
	
	        } catch (Exception e) {
	        	String errorMsg = "LoginPageController.moveToMain\n" + e.getMessage();
	        	IOHandler.getInstance().showAlert(errorMsg);
	        	IOHandler.getInstance().log(errorMsg);
	        }    	 
    }

    @FXML
    void OnSearchBtnClicked(ActionEvent event) {
    	IOHandler.getInstance().showAlert("테스트");
    }       
    
    //상품이 클릭되었을 때
    @FXML
    void OnProductClicked(MouseEvent event) {
    	if(event.getClickCount()>1)
    	{    		  	
    		// 사용자가 선택한 상품
    		Data selected = table.getSelectionModel().getSelectedItem();
    		if(selected == null) {
    			return;
    		}
    		Product target = selected.getProduct();    		
    		System.out.println(target.getName() + " clicked!");  
    		moveToProductPage(target);
    	}
    }
    
    // 새창에서 연다.
    private void moveToProductPage(Product p)
    {
    	 try {           
             Stage primaryStage = new Stage();
             FXMLLoader loader = new FXMLLoader(getClass().getResource("/page/ProductPage.fxml"));
             Parent root = loader.load();
             Scene scene = new Scene(root);
             
             ProductPageController pController = loader.getController();
             pController.DataTransfer(p);
             
             primaryStage.setScene(scene);
             primaryStage.setTitle(p.getName());
             primaryStage.show();

         } catch (Exception e) {
         	String errorMsg = "SearchPageController.moveToProductPager\n" + e.getMessage();
         	e.printStackTrace();
         	IOHandler.getInstance().showAlert(errorMsg);
         	IOHandler.getInstance().log(errorMsg);
         }
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		searchField.requestFocus();
	}
	
	public boolean transferProduct(ArrayList<Tuple<Product, CollectedInfo>> receievedList) {
		try {
			//밑에 부분이 init 부분으로 가면 왜 안되는지 아직 파악못함
        	ObservableList<Data> myList = FXCollections.observableArrayList();
        	
        	for(Tuple<Product, CollectedInfo> t : receievedList) {
        		// 상품정보 꺼냄
        		Product p = t.getFirst();
        		
        		// 상품의 수집정보(최신임)을 가져온다.
        		CollectedInfo ci = t.getSecond();
        		
        		Data newData = new Data(p, ci);
        		myList.add(newData);
        	}
        	     	
            ProductNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
            PriceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());    	
          
        	table.setItems(myList);
        	return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
    
	@Deprecated
	public boolean transferProduct(String s)
    {		
    	searchField.setText(s);    	
    	try {
    		searchField.requestFocus();
    		
    		// 서버 연결해서 상품명으로 검색하고, 결과 받아온다.
    		String searchWord = s;
    		
    		// 검색 시 서버로부터 상품정보 - 최신수집정보가 쌍(Tuple)으로 이루어진 결과 배열을 받음.
    		Protocol received = NetworkManager.getInstance().connect(ProtocolType.EVENT, EventType.SEARCH, (Object)searchWord);
        	Response response = received.getResponse();
        	ResponseType type = response.getResponseType();
        	
        	ArrayList<Tuple<Product, CollectedInfo>> receievedList = null;
        	
        	// 응답 결과에 따라 알아서 처리하셈.
        	switch(type) {
	        	case SUCCEED:
	        		receievedList = (ArrayList<Tuple<Product, CollectedInfo>>) received.getObject();
	        		break;
	        	case FAILED:
	        		break;
	        	case SERVER_NOT_RESPONSE:
	        		break;
	        	case ERROR:
	        		break;
        		default:
        			break;
        	}
        	
        	// 서버에서 받아온 값이 있을 때만 처리
        	if(receievedList == null || receievedList.size() < 1) {
        		IOHandler.getInstance().showAlert("검색 결과가 없습니다.");
        		return false;
        	}
        	
        	//밑에 부분이 init 부분으로 가면 왜 안되는지 아직 파악못함
        	ObservableList<Data> myList = FXCollections.observableArrayList();
        	
        	for(Tuple<Product, CollectedInfo> t : receievedList) {
        		// 상품정보 꺼냄
        		Product p = t.getFirst();
        		
        		// 상품의 수집정보(최신임)을 가져온다.
        		CollectedInfo ci = t.getSecond();
        		
        		Data newData = new Data(p, ci);
        		myList.add(newData);
        	}
        	     	
            ProductNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
            PriceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());    	
          
        	table.setItems(myList);
        	return true;
		}
		catch(Exception e)
		{
			IOHandler.getInstance().log("상품목록 초기화"+ e);
			e.printStackTrace();
			return false;
		}
    }
    
}


// 임의로 쓰는 데이터 객체, 테이블뷰에 쓰려면 StringProperty나 IntProperty 형식으로 써야함
// Product랑 CollectedInfo 저장하게 수정함. 수정할 일 없으면 final 쓰십쇼
class Data{
	private final Product product;
	private final CollectedInfo collectedInfo;
	
	private final StringProperty image;
	private final StringProperty name;
    private final StringProperty price; 
 
    public Data(final Product product, final CollectedInfo collectedInfo) {
    	this.product = product;
    	this.collectedInfo = collectedInfo;
    	
        this.name = new SimpleStringProperty(product.getName());
        this.image= new SimpleStringProperty("사진");
        this.price= new SimpleStringProperty(String.valueOf(collectedInfo.getPrice()));
    }
    
    public Product getProduct() {
    	return product;
    }
    
    public CollectedInfo getCollectedInfo() {
    	return this.collectedInfo;
    }
 
    public StringProperty nameProperty() {
        return name;
    }   
    public StringProperty imageProperty() {
		return image;
	}
    public StringProperty priceProperty() {
        return price;
    }
    public String getName() {
		return name.toString();
	}
   
 
}


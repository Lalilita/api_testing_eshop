package test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import pojo.LoginRequest;
import pojo.LoginResponse;
import pojo.OrderDetail;
import pojo.Orders;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;

public class ManageOrderAPI {

	public static void main(String[] args) {

	//Login
		// create request base
		RequestSpecification req = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
				.setContentType(ContentType.JSON).build();

		// create login request body
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUserEmail("lalilita888@gmail.com");
		loginRequest.setUserPassword("Buddysandies123!");

		// method given - all input details
		RequestSpecification reqLogin = given().log().all().spec(req).body(loginRequest);

		//create Response object
		    // method when - submit the API with resource and HTTP method
		LoginResponse loginResponse = reqLogin.when().post("api/ecom/auth/login")
		    //method then - validate the response		
				.then().log().all().extract().response().as(LoginResponse.class);
		
		System.out.println(loginResponse.getToken());
		String token = loginResponse.getToken();
		System.out.println(loginResponse.getUserId());
		String userId = loginResponse.getUserId();
		
		
	//Add Product
		//create request base
		RequestSpecification addProductBaseReq = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
				.addHeader("Authorization", token).build();
		//create request body
		RequestSpecification reqAddProduct = given().log().all().spec(addProductBaseReq).param("productName", "MISS GRAY")
				.param("productAddedBy", userId).param("productCategory", "fashion").param("productSubCategory", "shirts")
				.param("productPrice", "11500").param("productDescription", "My Brand").param("productFor", "women")
				.multiPart("productImage", new File("//Users/lalibee/Documents/Portfolio/tshirtForApi.jpg"));
		//create response body and we need productID, so we create string to use for JsonPath
		String addProductResponse = reqAddProduct.when().post("api/ecom/product/add-product")
				.then().log().all().assertThat().statusCode(201).body("message", equalTo("Product Added Successfully"))
				.extract().response().asString();
		JsonPath js = new JsonPath(addProductResponse);
		String productId = js.get("productId");
		
		
	//Create Order
		//create request base
		RequestSpecification createOrderBaseReq = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
				.addHeader("Authorization", token).setContentType(ContentType.JSON).build();
		//create request body
		OrderDetail orderDetail = new OrderDetail();
		orderDetail.setCountry("India");
		orderDetail.setProductOrderedId(productId);
		//create list to add the orderDetail 
		List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();
		orderDetailList.add(orderDetail);
		//and set into order
		Orders orders = new Orders();
		orders.setOrders(orderDetailList);
		//request body
		RequestSpecification reqCreateOrder = given().log().all().spec(createOrderBaseReq).body(orders);
		
		//create response body
		String responseCreateOrder = reqCreateOrder.when().post("api/ecom/order/create-order")
				.then().log().all().extract().response().asString();
		System.out.println(responseCreateOrder);
		JsonPath js1 = new JsonPath(responseCreateOrder);
		String ordersNumber = js1.getString("orders");
		ordersNumber = ordersNumber.substring(1,ordersNumber.length()-1);
		System.out.println(ordersNumber);
		
		
	//View Order Detail
		//create request base
		RequestSpecification viewOrderBaseReq = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
				.addHeader("Authorization", token).build();
		//create request body
		RequestSpecification reqViewOrder = given().log().all().spec(viewOrderBaseReq)
				.queryParam("id", ordersNumber);
		//create response body
		String responseViewOrderDetail = reqViewOrder.when().get("api/ecom/order/get-orders-details")
				.then().log().all().extract().response().asString();
		
		JsonPath js2 = new JsonPath(responseViewOrderDetail);
		Assert.assertEquals("Orders fetched for customer Successfully", js2.getString("message"));
		
		
	//Delete Product
		//create request base
		RequestSpecification deleteProductBaseReq = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
				.addHeader("Authorization", token).build();
		//create request body
		RequestSpecification reqDeleteProduct = given().log().all().spec(deleteProductBaseReq)
				.pathParam("productId", productId);
		//create response body
		String responseDeleteProduct = reqDeleteProduct.when().delete("api/ecom/product/delete-product/{productId}")
				.then().log().all().extract().response().asString();

		JsonPath js3 = new JsonPath(responseDeleteProduct);
		Assert.assertEquals("Product Deleted Successfully", js3.getString("message"));


	//Delete Order
		//create request base
		RequestSpecification deleteOrderBaseReq = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
				.addHeader("Authorization", token).build();
		//create request body
		RequestSpecification reqDeleteOrder = given().log().all().spec(deleteOrderBaseReq)
				.pathParam("ordersNumber", ordersNumber);
		//create response body
		String responseDeleteOrder = reqDeleteOrder.when().delete("api/ecom/order/delete-order/{ordersNumber}")
				.then().log().all().extract().response().asString();

		JsonPath js4 = new JsonPath(responseDeleteOrder);
		Assert.assertEquals("Orders Deleted Successfully", js4.getString("message"));

	}

}

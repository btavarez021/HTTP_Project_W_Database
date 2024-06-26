package org.example.Controller;

import io.javalin.http.Header;
import org.example.Exceptions.ProductException;
import org.example.Exceptions.SellerException;
import org.example.Model.Product;
import org.example.Model.Seller;
import org.example.Service.ProductService;
import org.example.Service.SellerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;

public class ProductController {

    static ProductService productService;
    static SellerService sellerService;

    public ProductController(ProductService productService, SellerService sellerService){
        ProductController.productService = productService;
        ProductController.sellerService = sellerService;
    }

    public Javalin getApi(){

        Javalin api = Javalin.create();

        { api.before (ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "*");
        });

            //Javalin to handle preflight requests (sent via OPTIONS)
            api.options("/*", ctx -> {
                ctx.header(Header.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000");
                ctx.header(Header.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
                ctx.header(Header.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization");
                ctx.status(200);
            });};

        api.get("/health", ctx -> {ctx.result("Server is up and running!");});

//        api.get("/seller", ctx ->{
//            List<Seller> sellerList = sellerService.getSellerList();
//            ctx.json(sellerList);
//        });
        api.get("seller", ProductController::getAllSellersHandler);
        api.get("/products", ProductController::getAllProductsHandler);
        api.get("/products/{productId}", ProductController::getProductById);

        api.post("/seller", ProductController::postSellerHandler);
        api.post("/products", ProductController::postProductHandler);

        api.put("/products/{productId}", ProductController::updateProductByIdHandler);

        api.delete("/products/{productId}", ProductController::deleteProductByIdHandler);
        api.delete("/seller/{sellerId}", ProductController::deleteSellerbyIdHander);

        return api;
    }

    public static void deleteProductByIdHandler(Context context){
        try {
            String productId = context.pathParam("productId").trim();
            productService.deleteProductById(productId);
            context.status(200);
        }
        catch(ProductException e){
            context.status(400);
            context.result(e.getMessage());
        }
    }

    public static void deleteSellerbyIdHander(Context context) {
        String sellerId = context.pathParam("sellerId").trim();
        sellerService.deleteSeller(sellerId);
        context.status(200);
    }

    public static void getAllProductsHandler(Context context){
        List<Product> productList = productService.getAllProducts();
        context.json(productList);
    }

    public static void getAllSellersHandler(Context context){
        List<Seller> sellerList = sellerService.getSellerList();
        context.json(sellerList);
    }

    public static void postProductHandler(Context context){

        ObjectMapper om =new ObjectMapper();

        try {
            String productId = productService.generateProductId();
            System.out.println("here is the created product ID " + productId);
//            long sellerId = sellerService.generateSellerId();
            Product p = om.readValue(context.body(), Product.class);
            productService.insertProduct(p, productId);
            context.status(201);
        }
        catch (JsonProcessingException | ProductException e){
            context.result(e.getMessage());
            context.status(400);
        }

    }

    public static void updateProductByIdHandler(Context context){
        ObjectMapper om =new ObjectMapper();

        try {
            String productId = context.pathParam("productId").trim();
            Product p = om.readValue(context.body(), Product.class);
            productService.updateProductById(p, productId);
            context.status(201);
        }
        catch (JsonProcessingException e){
            context.status(400);
        }
        catch(NumberFormatException e){
            context.status(400).result("Product ID is missing in the request");
        } catch (ProductException e) {
            context.status(400).result(e.getMessage());
        }

    }

    public static void postSellerHandler(Context context){
        ObjectMapper om =new ObjectMapper();

        try {
            String sellerId = sellerService.generateSellerId();
            Seller s = om.readValue(context.body(), Seller.class);
            sellerService.postSeller(s, sellerId);
            context.status(201);

        } catch (SellerException | JsonProcessingException e) {
            context.result(e.getMessage());
            context.status(400);
        }

    }

    public static void getProductById(Context context){
        try{
            String productId = context.pathParam("productId");
            Product p = productService.getProductById(productId);
            if(p == null){
                context.status(404);
            }
            else{
                context.json(p);
                context.status(200);
            }
        }
        catch(NumberFormatException e){
            context.status(404);
            context.result("Invalid Product Id");
        }
    }

}

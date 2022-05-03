package lk.ijse.dep8.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep8.api.dto.cartDTO;
import lk.ijse.dep8.api.exception.ValidationException;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "CartServlet", value = {"/cart","/cart/*"})
public class CartServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4Pos")
    public volatile DataSource pool;
    public void doSaveOrUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getContentType()== null || !req.getContentType().toLowerCase().startsWith("application/json")){
            res.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }
        String method = req.getMethod();
        String pathInfo = req.getPathInfo();

        if (method.equals("POST") && (pathInfo!=null && !pathInfo.equals("/"))){
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (method.equals("PUT") && !(pathInfo != null &&
                pathInfo.substring(1).matches("\\d{5}"))) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Item does not exist");
            return;
        }

        try(Connection con = pool.getConnection()){
            Jsonb jsonb = JsonbBuilder.create();
            cartDTO cart = jsonb.fromJson(req.getReader(), cartDTO.class);
            PreparedStatement stm = con.prepareStatement("SELECT * FROM item WHERE itemCode=?");
            stm.setString(1,cart.getItemCode());
            ResultSet rst = stm.executeQuery();
            if (!rst.next()){
                throw new ValidationException("Item does not exists");
            }
            if (rst.getInt("qty")>=Integer.parseInt(cart.getAmount())){
                throw new RuntimeException("Available quantity of"+rst.getString("itemName")+"is: "+rst.getString("qty"));
            }

            PreparedStatement stm2 = con.prepareStatement("SELECT * FROM cart WHERE itemCode=?");
            stm2.setString(1,cart.getItemCode());
            ResultSet rst2 = stm2.executeQuery();
            if (!rst2.next()){
                PreparedStatement stm3 = con.prepareStatement("UPDATE cart SET amount=?, price=?");
                stm3.setInt(1,rst2.getInt("amount")+Integer.parseInt(cart.getAmount()));
                stm3.setDouble(2,rst2.getDouble("price")+Double.parseDouble(cart.getPrice()));
                int i = stm3.executeUpdate();
                if (i!=1){
                    throw new RuntimeException("Can not update the data!");
                }
            }else {
                PreparedStatement stm4 = con.prepareStatement("INSERT INTO cart (itemCode, customerId, amount, price) VALUES (?,?,?,?)");
                stm4.setString(1,cart.getItemCode());
                stm4.setString(2,cart.getCustomerId());
                stm4.setInt(3,Integer.parseInt(cart.getAmount()));
                stm4.setDouble(4,Double.parseDouble(cart.getPrice()));
                int i = stm4.executeUpdate();
                if (i!=1){
                    throw new RuntimeException("Can not update the data!");
                }
                res.setStatus(HttpServletResponse.SC_CREATED,"Success!");
            }
        }catch (ValidationException | RuntimeException e){
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Throwable e){
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
        }


    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doSaveOrUpdate(request,response);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        doSaveOrUpdate(req,resp);
    }
}

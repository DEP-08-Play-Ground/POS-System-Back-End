package lk.ijse.dep8.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep8.api.dto.itemDTO;
import lk.ijse.dep8.api.exception.ValidationException;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;

@MultipartConfig(location = "/tmp",maxFileSize = 1024*1024*5)
@WebServlet(name = "lk.ijse.dep8.api.itemServlet", value = {"/items","/items/*"})
public class itemServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4Pos")
    public volatile DataSource pool;
    private void doSaveOrUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getContentType()== null || !req.getContentType().toLowerCase().startsWith("multipart/form-data")){
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
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Book does not exist");
            return;
        }

        try{
            String itemCode =req.getParameter("itemCode");
            String itemName =req.getParameter("itemName");
            String price =req.getParameter("price");
            String qty =req.getParameter("qty");
            Part preview =req.getPart("preview");

            itemDTO item;
            if (preview!=null && !preview.getSubmittedFileName().isEmpty()){
                if (!preview.getContentType().toLowerCase().startsWith("image/")){
                    throw new ValidationException("Invalid image type");
                }

                InputStream is = preview.getInputStream();
                byte[] buffer = new byte[(int) preview.getSize()];
                is.read(buffer);
                item = new itemDTO(itemCode, itemName, price, qty, buffer);
            }else {
                item = new itemDTO(itemCode, itemName, price, qty);
            }

            if (method.equals("PUT")){
                item.setItemCode(pathInfo.replaceAll("[/]",""));
            }

            if (!item.getItemCode().matches("\\d{5}")) {
                throw new ValidationException("Invalid ID");
            } else if (item.getItemName()==null) {
                throw new ValidationException("Should add the book name");
            } else if (item.getPrice()==null && !item.getPrice().matches("\\d{2,}")) {
                throw new ValidationException("Invalid Price");
            } else if (item.getQty()==null && !item.getQty().matches("\\d{2,}")) {
                throw new ValidationException("Invalid Qty");
            }

            try (Connection con = pool.getConnection()) {
                PreparedStatement stm = con.prepareStatement("SELECT * FROM item WHERE itemCode=?");
                stm.setString(1,item.getItemCode());
                ResultSet resultSet = stm.executeQuery();
                if (resultSet.next()) {
                    if (method.equals("POST")) {
                        res.sendError(HttpServletResponse.SC_CONFLICT, "Item already exists!");
                    } else {
                        PreparedStatement stm1 = con.prepareStatement("UPDATE item SET itemName=?, price=?, qty=?,preview=? WHERE itemCode=?");
                        stm1.setString(1,item.getItemName());
                        stm1.setString(2,item.getPrice());
                        stm1.setString(3,item.getQty());
                        stm1.setBlob(4,item.getPreview()==null? null:new SerialBlob(item.getPreview()));
                        stm1.setString(5,item.getItemCode());
                        if (stm1.executeUpdate()!=1){
                            throw new RuntimeException("Failed to Update the Item!");
                        }
                        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }

                }else {
                    PreparedStatement stm2 = con.prepareStatement("INSERT INTO item(itemCode, itemName, price, qty, preview) VALUES (?,?,?,?,?)");
                    stm2.setString(1, item.getItemCode());
                    stm2.setString(2, item.getItemName());
                    stm2.setDouble(3, Double.parseDouble(item.getPrice()));
                    stm2.setString(4, item.getQty());
                    stm2.setBlob(5, item.getPreview()==null? null:new SerialBlob(item.getPreview()));
                    int i = stm2.executeUpdate();
                    if (i != 1) {
                        throw new RuntimeException("Failed to save the Item!");
                    }
                    res.setStatus(HttpServletResponse.SC_CREATED);
                    res.getWriter().write("Successfully saved the Item!");
                }
            }

        }catch (ValidationException e){
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());
        } catch (Throwable e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo() != null && !(req.getPathInfo().equals("/id") || req.getPathInfo().equals("/"))) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String query = req.getParameter("q");
        query = "%" + ((query == null) ? "" : query) + "%";
        boolean pagination = req.getParameter("page") != null && req.getParameter("size") != null;
        String sql;
        if (pagination) {
            sql = "SELECT * FROM item WHERE item.itemCode LIKE ? OR item.itemName LIKE ? OR item.price LIKE ? OR item.qty LIKE ? LIMIT ? OFFSET ?";
        } else {
            sql = "SELECT * FROM item WHERE item.itemCode LIKE ? OR item.itemName LIKE ? OR item.price LIKE ? OR item.qty LIKE ?";
        }

        String s = "";
        if (req.getPathInfo() != null) {
            s = req.getPathInfo().replaceAll("/", "");
        }
        if (!s.equals("")) {
            try (Connection con = pool.getConnection()) {
                PreparedStatement stm = con.prepareStatement("SELECT * FROM item WHERE itemCode=?");
                stm.setString(1, s);
                ResultSet rst = stm.executeQuery();
                if (rst.next()) {
                    resp.getWriter().write("Item is not available!");
                    return;
                } else {
                    resp.getWriter().write("Item is available!");
                    return;
                }
            } catch (SQLException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }

        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement(sql);
            stm.setString(1, query);
            stm.setString(2, query);
            stm.setString(3, query);
            stm.setString(4, query);
            if (pagination) {
                int page = Integer.parseInt(req.getParameter("page"));
                int size = Integer.parseInt(req.getParameter("size"));
                stm.setInt(5, size);
                stm.setInt(6, (page - 1) * size);
            }
            ResultSet rst = stm.executeQuery();
            Jsonb jsonb = JsonbBuilder.create();
            ArrayList<itemDTO> bookDTOS = new ArrayList<>();
            while (rst.next()) {
                bookDTOS.add(new itemDTO(rst.getString("itemCode"), rst.getString("itemName")
                        , rst.getString("price"), rst.getString("qty"), rst.getBytes("preview")));
            }

            resp.setContentType("application/json");
            jsonb.toJson(bookDTOS, resp.getWriter());
            bookDTOS.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Failed to fetch data");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doSaveOrUpdate(request,response);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doSaveOrUpdate(req,resp);
    }
}

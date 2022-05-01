package lk.ijse.dep8.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep8.api.dto.CustomerDTO;
import lk.ijse.dep8.api.exception.ValidationException;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "CorsFilter", urlPatterns = {"/customers/*"})
public class CustomerServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4Pos")
    public volatile DataSource pool;

    private void doSaveorUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getPathInfo() != null && !req.getPathInfo().replaceAll("/", "").matches("C\\d{3}")) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "page not found!");
            return;
        } else if (req.getContentType() == null || !req.getContentType().equals("application/json")) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } else if (req.getPathInfo()==null && req.getMethod().equals("PUT")) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try (Connection connection = pool.getConnection()) {

            Jsonb jsonb = JsonbBuilder.create();
            CustomerDTO dto = jsonb.fromJson(req.getReader(), CustomerDTO.class);
            String id = req.getPathInfo() == null ? null : req.getPathInfo().replaceAll("/", "");
            if (req.getMethod().equals("PUT")) dto.setId(id);

            if (!dto.getId().matches("C\\d{3}")) {
                throw new ValidationException("Invalid Id");
            } else if (!dto.getName().matches("[A-Za-z]+")) {
                throw new ValidationException("Invalid Name");
            } else if (!dto.getAddress().matches("[A-Za-z\\d]{5,}")) {
                throw new ValidationException("Invalid Address");
            } else if (!dto.getNic().matches("\\d{9}[Vv]")) {
                throw new ValidationException("Invalid NIC");
            }
            String sql;
            if (req.getMethod().equals("POST")) {
                PreparedStatement rgst = connection.prepareStatement("SELECT * FROM customer WHERE id=?");
                rgst.setString(1,dto.getId());
                ResultSet rst = rgst.executeQuery();
                if (rst.next()){
                    res.sendError(HttpServletResponse.SC_CONFLICT,"Already has a member with this Id");
                    return;
                }
                sql = "INSERT INTO customer (id, name, address, nic) VALUES (?,?,?,?)";
            } else {
                PreparedStatement rgst = connection.prepareStatement("SELECT * FROM customer WHERE id=?");
                rgst.setString(1,dto.getId());
                ResultSet rst = rgst.executeQuery();
                if (!rst.next()) {
                    res.sendError(HttpServletResponse.SC_NOT_FOUND, "Can not find the Customer");
                    return;
                }

                sql = "UPDATE customer SET name=?,address=?,nic=? WHERE id=?";
            }
            PreparedStatement stm = connection.prepareStatement(sql);
            if (req.getMethod().equals("POST")) {
                System.out.println("POST");
                stm.setString(1, dto.getId());
                stm.setString(2, dto.getName());
                stm.setString(3, dto.getAddress());
                stm.setString(4, dto.getNic());
            } else {
                System.out.println("PUT");
                stm.setString(1, dto.getName());
                stm.setString(2, dto.getAddress());
                stm.setString(3, dto.getNic());
                stm.setString(4, dto.getId());
            }
            int i = stm.executeUpdate();
            if (i != 1) {
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            } else {
                res.setStatus(HttpServletResponse.SC_CREATED);
                res.getWriter().write("Success!!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (ValidationException e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (JsonbException e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Throwable e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doSaveorUpdate(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doSaveorUpdate(req, resp);
    }
}

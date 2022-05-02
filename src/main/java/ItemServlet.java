import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@MultipartConfig(location = "/tmp",maxFileSize = 1024*1024*5)
@WebServlet(name = "ItemServlet", value = "/ItemServlet")
public class ItemServlet extends HttpServlet {

    private void doSaveOrUpdate(HttpServletRequest req, HttpServletResponse res){

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
        doSaveOrUpdate(req,resp);
    }
}

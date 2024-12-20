package com.ecommerce.control;

import com.ecommerce.dao.AccountDao;
import com.ecommerce.dao.CustomerAccountBalanceDao;
import com.ecommerce.dao.OrderDao;
import com.ecommerce.dao.ProductDao;
import com.ecommerce.entity.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(name = "CheckoutControl", value = "/checkout")
public class CheckoutControl extends HttpServlet {
    // Call DAO class to access with database.
    OrderDao orderDao = new OrderDao();
    AccountDao accountDao = new AccountDao();

    CustomerAccountBalanceDao balanceDao = new CustomerAccountBalanceDao();
    ProductDao productDao = new ProductDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        // Get information from input field.
        String firstName = request.getParameter("first-name");
        String lastName = request.getParameter("last-name");
        String address = request.getParameter("address");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        if (session.getAttribute("account") == null) {
            response.sendRedirect("login.jsp");
        }
        else {
            double totalPrice = (double) session.getAttribute("total_price");
            Order order = (Order) session.getAttribute("order");
            Account account = (Account) session.getAttribute("account");

            // Insert information to account.
            int accountId = account.getId();
            accountDao.updateProfileInformation(accountId, firstName, lastName, address, email, phone);
            // Insert order to database.
            orderDao.createOrder(account.getId(), totalPrice, order.getCartProducts());

            for (CartProduct cart: order.getCartProducts()) {
                int previousQuantity = cart.getProduct().getAmount();
                int updatedQuantity = previousQuantity - cart.getQuantity();
                productDao.updateProductAmount(cart.getProduct().getId(), updatedQuantity);
            }

            CustomerAccountBalance balance = balanceDao.getAccountBalanceByAccountNumber(account.getAccountNumber());

            double remainingBalance = balance.getBalance() - (totalPrice);

            balanceDao.updateBalance(account.getAccountNumber(), remainingBalance);

            session.removeAttribute("order");
            session.removeAttribute("total_price");

            RequestDispatcher requestDispatcher = request.getRequestDispatcher("thankyou.jsp");
            requestDispatcher.forward(request, response);
        }
    }
}

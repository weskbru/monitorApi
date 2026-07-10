import { renderDashboardPage } from "../pages/dashboard/dashboard.js";
import { renderRegisterPage } from "../pages/register/register.js";

const appRoot = document.querySelector("#app-root");
const routeLinks = document.querySelectorAll("[data-route]");

const routes = {
  dashboard: renderDashboardPage,
  register: renderRegisterPage,
};

function setRoute(routeName) {
  const renderer = routes[routeName] ?? routes.dashboard;

  routeLinks.forEach((link) => {
    link.classList.toggle("active", link.dataset.route === routeName);
  });

  renderer(appRoot);
  appRoot.focus({ preventScroll: true });
}

routeLinks.forEach((link) => {
  link.addEventListener("click", (event) => {
    event.preventDefault();
    const routeName = link.dataset.route;

    history.replaceState(null, "", link.getAttribute("href"));
    setRoute(routeName);
  });
});

const initialRoute = window.location.hash === "#cadastro" ? "register" : "dashboard";

setRoute(initialRoute);

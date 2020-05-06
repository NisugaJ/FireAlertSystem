const isLogged = () => {
  if (typeof window !== "undefined") {
    console.log("app is running on the client")
  } else {
    console.log("app is running on the server")
  }
  return sessionStorage.getItem("admin")
    ? true
    : false

  // return true
  // return false
}

const logOut = () => {
  console.log("Clearingggg")
  sessionStorage.clear()
  // sessionStorage.setItem("user", "")
  // sessionStorage.setItem("access_token", "")
  return isLogged()
}

export { isLogged, logOut }

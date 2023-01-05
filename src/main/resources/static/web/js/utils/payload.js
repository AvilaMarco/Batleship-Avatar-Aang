const postToken = (token) => ({
  method: "POST",
  headers: {
    Authorization: token,
  },
});

const getToken = (token) => ({
  method: "GET",
  headers: {
    Authorization: token,
  },
});

const postBody = (object) => ({
  method: "POST",
  body: JSON.stringify(object),
  headers: {
    "Content-Type": "application/json",
  },
});

export { postBody, getToken, postToken };

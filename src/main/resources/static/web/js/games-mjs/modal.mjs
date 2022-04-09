function hideModal() {
  const modal = document.querySelector("#modal");
  modal.classList.add("modalAnimationout");
  modal.classList.add("hidden");
  modal.classList.remove("modalAnimation");
  document.querySelector("#container").style.opacity = 1;
}

function showModal() {
  const modal = document.querySelector("#modal");
  modal.classList.remove("hidden");
  modal.classList.remove("modalAnimationout");
  modal.classList.add("modalAnimation");
  document.querySelector("#container").style.opacity = 0.2;
}

function cleanModal(nation) {
  let modalDiv = document.querySelector(".div-modal");
  modalDiv.innerHTML = "";
  modalDiv.classList.remove("bg-" + nation);
  modalDiv.classList.remove("bg-solid-" + nation);
}

export { hideModal, showModal, cleanModal };

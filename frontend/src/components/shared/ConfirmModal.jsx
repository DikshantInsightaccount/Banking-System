export default function ConfirmModal({
  id,
  title = "Confirm",
  body = "Are you sure?",
  confirmText = "Confirm",
  confirmClass = "btn btn-danger",
  onConfirm,
}) {
  return (
    <div className="modal fade" id={id} tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h6 className="modal-title">{title}</h6>
            <button type="button" className="btn-close" data-bs-dismiss="modal" />
          </div>
          <div className="modal-body">{body}</div>
          <div className="modal-footer">
            <button className="btn btn-light" data-bs-dismiss="modal">Cancel</button>
            <button
              className={confirmClass}
              data-bs-dismiss="modal"
              onClick={onConfirm}
            >
              {confirmText}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
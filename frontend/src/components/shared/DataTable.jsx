import EmptyState from "./Emptystate";

export default function DataTable({ columns, rows, rowKey, onRowClick }) {
  if (!rows || rows.length === 0) {
  return (
    <div className="alert alert-secondary mt-3">
      No records found
    </div>
  );
}

  return (
    <div className="table-responsive bg-white border rounded">
      <table className="table table-hover mb-0">
        <thead className="table-light">
          <tr>
            {columns.map((c) => (
              <th key={c.key} className={c.className || ""}>
                {c.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr
              key={rowKey(r)}
              role={onRowClick ? "button" : undefined}
              onClick={() => onRowClick?.(r)}
            >
              {columns.map((c) => (
                <td key={c.key} className={c.className || ""}>
                  {c.render ? c.render(r) : r[c.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
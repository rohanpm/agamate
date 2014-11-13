(ns agamate.transactions
  (:require immutant.transactions
            korma.db))

(set! *warn-on-reflection* true)

(defmacro transaction [& body]
  "Executes body within a transaction.
   Actually, as we don't use an XA datasource for DB access, two
   transactions are used."
  `(immutant.transactions/transaction
    (korma.db/transaction
     ~@body)))

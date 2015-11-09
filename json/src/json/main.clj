(ns json.main
	(:require [cheshire.core :refer :all]))

(defn jsonalike2seq
	"load origin json like file content into map seq"
	[file]
	(map #(parse-string % true) (clojure.string/split (slurp file) #"#\n")))

(defn seq2json
	"convert map seq from func jsonalike2seq to json string"
	[x]
	(generate-string {:abstract x}))

(defn json2map
	"load json data into map"
	[json]
	(parse-string json true))

(defn transfer
	"one argument for outer json and two for inner json"
	([mapdata]
		(let [res (transient {})]
			(doseq [[ke cont] mapdata]
				(let [rows (transient [])  resitem (transient {})]
					(doseq [item cont]
						(let [parent (:id item) heads (transient {}) ritem (transient {})]
							(doseq [[k c] item]
								(if (coll? c)
									(let [child (transfer c parent)]
										(if (k res)
											(doseq [row (:rows child)]
												(conj! (:rows (k res)) row))
											(let [cmap (transient {}) crows (transient [])]
												(assoc! cmap :head (:head child))
												(doseq [row (:rows child)]
													(conj! crows row))
												(assoc! cmap :rows crows)
												(assoc! res k cmap))))
									(do
										(assoc! heads k 0)
										(assoc! ritem k c))))
							(if (ke res)
								(conj! (:rows (ke res)) (persistent! ritem))
								(do 
									(conj! rows (persistent! ritem))
									(assoc! resitem :head (persistent! heads))
									(assoc! resitem :rows rows)
									(assoc! res ke resitem)))))))
			(let [pres (persistent! res) p (transient {})]
				(doseq [[k c] pres]
					(let [pcont (transient {})]
						(assoc! pcont :head (:head c))
						(assoc! pcont :rows (persistent! (:rows c)))
						(assoc! p k (persistent! pcont))))
				(persistent! p))))
	([mapdata parent]
		(let [cmap (transient {}) heads (transient {}) rows (transient [])]
			(doseq [cont mapdata]
				(let [row (transient {})]
					(do
						(doseq [[k c] cont]
							(do
								(assoc! heads k 0)
								(assoc! row k c)))
						(assoc! row :parent parent)
						(conj! rows (persistent! row)))))
			(assoc! cmap :head (persistent! heads))
			(assoc! cmap :rows (persistent! rows))
			(persistent! cmap))))

(defn -main
	([]
		(let [x (transfer (json2map (seq2json (jsonalike2seq "/tmp/foo"))))]
			(spit "/tmp/bar" (generate-string x {:pretty true}))))
	([infile outfile]
		(let [x (transfer (json2map (seq2json (jsonalike2seq infile))))]
			(spit outfile (generate-string x {:pretty true})))))